package com.distributed.p2pFileTransfer;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FreeNetFileTransferService extends AbstractFileTransferService {

  private static FreeNetFileTransferService instance;
  private ExecutorService executorService;

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

  private FreeNetFileTransferService(FileHandler fileHandler, int port, Node boostrapServer)
      throws SocketException, UnknownHostException, NodeNotFoundException {
    super(fileHandler, port, boostrapServer);
    this.executorService = Executors.newCachedThreadPool();
  }

  @Override
  public Future<List<String>> searchForFile(String query) {
    Callable<List<String>> searchExecutor =
        () -> {
          List<Node> neighbours = new LinkedList<>();
          this.getNetwork().getNeighbours().forEachRemaining(neighbours::add);
          List<Query> queries =
              Query.createQuery(this.getCommandBuilder().getSearchCommand(query), neighbours);
          List<QueryResult> results =
              this.getQueryDispatcher().dispatchAll(queries).stream()
                  .map(
                      each -> {
                        try {
                          return each.get();
                        } catch (InterruptedException | ExecutionException e) {
                          e.printStackTrace();
                        }
                        return null;
                      })
                  .collect(Collectors.toList());
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
          return new ArrayList<>(files);
        };
    return executorService.submit(searchExecutor);
  }

  @Override
  public void downloadFile(String fileName)
      throws FileNotFoundException, DestinationAlreadyExistsException {}

  @Override
  public void downloadFileFrom(String fileName,Node source)
      throws FileNotFoundException, DestinationAlreadyExistsException, NodeNotFoundException {
      getFileHandler().downloadFileToLocal(source,fileName);
  }

  @Override
  void stop() {
    super.stop();
  }
}
