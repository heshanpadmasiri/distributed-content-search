package com.distributed.p2pFileTransfer;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Future;

public abstract class AbstractFileTransferService {
  private Network network;
  private FileHandler fileHandler;
  private QueryDispatcher queryDispatcher;
  private QueryListener queryListener;
  private CommandBuilder commandBuilder;

  /**
   * Used to search for a file. This will flood the network and return all matching <b>unique</b>
   * files. File is considered unique based on the file name
   *
   * @param query what to search for
   * @return list of files matching the search query
   */
  public abstract Future<List<String>> searchForFile(String query);

  /**
   * Used to download a file. Will download the first file that exactly matches the file name.
   *
   * @param fileName Name of the file to download
   * @param destination Path representing the destination file
   * @throws FileNotFoundException If no file is found that exactly matches the file name
   * @throws DestinationAlreadyExistsException If the destination file already exists. <b>This
   *     method will not overwrite existing files</b>
   */
  public abstract void downloadFile(String fileName, Path destination)
      throws FileNotFoundException, DestinationAlreadyExistsException;

  /**
   * Used to directly download a file without search for it in the network
   *
   * @param fileName Name of the file to download
   * @param destination Path representing the destination file
   * @param source Node from which to download the file
   * @throws FileNotFoundException If no file is found that exactly matches the file name in the
   *     source
   * @throws DestinationAlreadyExistsException If the destination file already exists. <b>This
   *     method will not overwrite existing files</b>
   * @throws NodeNotFoundException If source node refused to connect
   */
  public abstract void downloadFileFrom(String fileName, Path destination, Node source)
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
}
