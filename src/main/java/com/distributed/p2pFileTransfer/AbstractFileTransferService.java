package com.distributed.p2pFileTransfer;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Future;

public abstract class AbstractFileTransferService {
  private final Network network;
  private final FileHandler fileHandler;
  private final QueryDispatcher queryDispatcher;
  private final QueryListener queryListener;
  private final CommandBuilder commandBuilder;
  private Node currentNode;
  private Thread queryListenerThread;

  public AbstractFileTransferService(FileHandler fileHandler, int port, Node bootstrapServer) throws SocketException, UnknownHostException, NodeNotFoundException {
    this.fileHandler = fileHandler;
    this.queryListener = new QueryListener(this, port);
    this.queryDispatcher = new QueryDispatcher(this);
    this.currentNode = new Node(port);
    this.commandBuilder = CommandBuilder.getInstance(currentNode);
    queryListenerThread = new Thread(this.queryListener);
    queryListenerThread.start();
    this.network = new Network(this, bootstrapServer);
  }

  /**
   * Used to search for a file. This will flood the network and return all matching <b>unique</b>
   * files. File is considered unique based on the file name
   *
   * @param query what to search for
   * @return list of files matching the search query
   */
  public abstract Future<List<String>> searchForFile(String query);

  /**
   * Use to flood the network with a query and get the response
   * @param queryBody Body of the query
   * @return results of flooding the network
   */
  protected abstract Future<List<QueryResult>> floodNetwork(String queryBody);


  /**
   * Used to download a file. Will download the first file that exactly matches the file name.
   *
   * @param fileName Name of the file to download
   * @throws FileNotFoundException If no file is found that exactly matches the file name
   * @throws DestinationAlreadyExistsException If the destination file already exists. <b>This
   *     method will not overwrite existing files</b>
   */
  public abstract void downloadFile(String fileName)
      throws FileNotFoundException, DestinationAlreadyExistsException;

  /**
   * Used to directly download a file without search for it in the network
   *
   * @param fileName Name of the file to download
   * @param source Node from which to download the file
   * @throws FileNotFoundException If no file is found that exactly matches the file name in the
   *     source
   * @throws DestinationAlreadyExistsException If the destination file already exists. <b>This
   *     method will not overwrite existing files</b>
   * @throws NodeNotFoundException If source node refused to connect
   */
  public abstract void downloadFileFrom(String fileName, Node source)
      throws FileNotFoundException, DestinationAlreadyExistsException, NodeNotFoundException;

  Network getNetwork() {
    return network;
  }

  FileHandler getFileHandler() {
    return fileHandler;
  }

  QueryDispatcher getQueryDispatcher() {
    return queryDispatcher;
  }

  QueryListener getQueryListener() {
    return queryListener;
  }

  CommandBuilder getCommandBuilder() {
    return commandBuilder;
  }

  void stop(){
    queryListener.stop();
    try {
      queryListenerThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
