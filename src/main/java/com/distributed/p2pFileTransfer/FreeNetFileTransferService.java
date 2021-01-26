package com.distributed.p2pFileTransfer;

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
  private Thread queryListenerThread;

  public static synchronized FreeNetFileTransferService getInstance(Properties configuration, Network network)
          throws SocketException, UnknownHostException {
    if (instance == null) {
      // todo: replace with concrete implementation
      FileHandler fileHandler =
          new FileHandler(
              configuration.getProperty("cache_dir"),
              configuration.getProperty("local_dir"),
              Integer.parseInt(configuration.getProperty("cache_size")),
                  configuration.getProperty("server_port"));
      instance =
          new FreeNetFileTransferService(
              network, fileHandler, Integer.parseInt(configuration.getProperty("port")));
    }
    return instance;
  }

  private FreeNetFileTransferService(Network network, FileHandler fileHandler, int port)
          throws SocketException, UnknownHostException {
    super(network, fileHandler, port);
    this.executorService = Executors.newCachedThreadPool();
    queryListenerThread = new Thread(this.getQueryListener());
    queryListenerThread.start();
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
                        } catch (InterruptedException e) {
                          e.printStackTrace();
                        } catch (ExecutionException e) {
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
  public void downloadFile(String fileName, Path destination)
      throws FileNotFoundException, DestinationAlreadyExistsException {}

  @Override
  public void downloadFileFrom(String fileName, Path destination, Node source)
      throws FileNotFoundException, DestinationAlreadyExistsException, NodeNotFoundException {}

    @Override
    void stop() {
        super.stop();
        this.getQueryListener().stop();
        try {
            this.queryListenerThread.join(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
