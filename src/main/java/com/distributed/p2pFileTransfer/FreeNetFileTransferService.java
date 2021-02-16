package com.distributed.p2pFileTransfer;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FreeNetFileTransferService extends AbstractFileTransferService {

  private static FreeNetFileTransferService instance;
  private ExecutorService executorService;

  private FreeNetFileTransferService(FileHandler fileHandler, int port, Node boostrapServer)
      throws SocketException, UnknownHostException, NodeNotFoundException {
    super(fileHandler, port, boostrapServer);
    this.executorService = Executors.newCachedThreadPool();
  }

  public static synchronized FreeNetFileTransferService getInstance(Properties config)
      throws SocketException, UnknownHostException, NodeNotFoundException {
    if (instance == null) {
      Configuration.setConfiguration(config);
      FileHandler fileHandler =
          new FileHandler(
              Configuration.getCacheDir(),
              Configuration.getLocalDir(),
              Configuration.getCacheSize(),
              Configuration.getPort());
      Node bootstrapServer =
          new Node(
              InetAddress.getByName(Configuration.getBootstrapServerIp()),
              Integer.parseInt(Configuration.getBootstrapServerport()));

      instance =
          new FreeNetFileTransferService(
              fileHandler, Integer.parseInt(Configuration.getPort()), bootstrapServer);
    }
    return instance;
  }

  @Override
  public Future<List<String>> searchForFile(String query) {
    Callable<List<String>> searchExecutor =
        () -> {
          String queryBody = getCommandBuilder().getSearchCommand(query);
          List<QueryResult> results = floodNetwork(queryBody).get();
          Set<String> files = new TreeSet<>();
          results.forEach(
              result -> {
                String[] data = result.getBody().split(" ");
                int fileCount = Integer.parseInt(data[2]);
                if (fileCount > 0) {
                  IntStream.range(6, 6 + fileCount)
                      .forEach(
                          idx -> {
                            files.add(data[idx]);
                          });
                }
              });
          return files.stream().map(each -> each.replaceAll("_", " ")).collect(Collectors.toList());
        };
    return executorService.submit(searchExecutor);
  }

  @Override
  protected Future<List<QueryResult>> floodNetwork(String queryBody) {
    Callable<List<QueryResult>> floodExecutor =
        () -> {
          List<Node> neighbours = new LinkedList<>();
          this.getNetwork().getNeighbours().forEachRemaining(neighbours::add);
          List<Query> queries = Query.createQuery(queryBody, neighbours);
          List<QueryResult> results =
              this.getQueryDispatcher().dispatchAll(queries).stream()
                  .map(
                      each -> {
                        try {
                          return each.get(20, TimeUnit.SECONDS);
                        } catch (TimeoutException ignored) {
                          logger.log(Level.WARNING, "Query time out");
                        } catch (InterruptedException | ExecutionException e) {
                          e.printStackTrace();
                        }
                        return null;
                      })
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
          return results;
        };
    return executorService.submit(floodExecutor);
  }

  @Override
  public Future<FileDownloadResult> downloadFile(String fileName) {
    String queryBody = getCommandBuilder().getSearchCommand(fileName);
    Callable<QueryResult> fileFinder =
        () -> {
          List<Node> neighbours = new LinkedList<>();
          this.getNetwork().getNeighbours().forEachRemaining(neighbours::add);
          List<Query> queries = Query.createQuery(queryBody, neighbours);
          for (Query query : queries) {
            try {
              Future<QueryResult> future = this.getQueryDispatcher().dispatchOne(query);
              QueryResult result = future.get(20, TimeUnit.SECONDS);
              String[] data = result.getBody().split(" ");
              int numberOfFiles = Integer.parseInt(data[2]);
              if (numberOfFiles > 0) {
                for (int i = 0; i < numberOfFiles; i++) {
                  String name = data[6 + i].replaceAll("_", " ");
                  if (name.equals(fileName)) {
                    return result;
                  }
                }
              }
            } catch (TimeoutException ex) {
              logger.log(Level.WARNING, "Query time out");
              this.getNetwork().removeNeighbour(query.destination);
            }
          }
          return null;
        };
    try {
      QueryResult result = executorService.submit(fileFinder).get();
      if (result != null) {
        String[] data = result.getBody().split(" ");
        Node source = new Node(InetAddress.getByName(data[3]), Integer.parseInt(data[4]));
        return getFileHandler().downloadFileToLocal(source, fileName);
      }
    } catch (InterruptedException | ExecutionException | UnknownHostException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Future<FileDownloadResult> downloadFileFrom(String fileName, Node source)
      throws DestinationAlreadyExistsException, NodeNotFoundException {
    return getFileHandler().downloadFileToLocal(source, fileName);
  }

  @Override
  void stop() {
    super.stop();
  }
}
