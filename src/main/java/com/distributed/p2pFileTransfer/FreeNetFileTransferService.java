package com.distributed.p2pFileTransfer;

import java.net.SocketException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

public class FreeNetFileTransferService extends AbstractFileTransferService {

  private static FreeNetFileTransferService instance;

  public static synchronized FreeNetFileTransferService getInstance(Properties configuration) throws SocketException {
    if (instance == null) {
      // todo: replace with concrete implementation
      Network network = null;
      FileHandler fileHandler =
          new FileHandler(
              configuration.getProperty("cache_dir"),
              configuration.getProperty("local_dir"),
              Integer.parseInt(configuration.getProperty("cache_size")));
      instance =
          new FreeNetFileTransferService(
              network, fileHandler, Integer.parseInt(configuration.getProperty("port")));
    }
    return instance;
  }

  private FreeNetFileTransferService(
      Network network,
      FileHandler fileHandler,
      int port) throws SocketException {
    super(network, fileHandler, port);
  }

  @Override
  public Future<List<String>> searchForFile(String query) {
    return null;
  }

  @Override
  public void downloadFile(String fileName, Path destination)
      throws FileNotFoundException, DestinationAlreadyExistsException {}

  @Override
  public void downloadFileFrom(String fileName, Path destination, Node source)
      throws FileNotFoundException, DestinationAlreadyExistsException, NodeNotFoundException {}
}
