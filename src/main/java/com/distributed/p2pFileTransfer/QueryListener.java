package com.distributed.p2pFileTransfer;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

class QueryListener implements Runnable {
  private final AbstractFileTransferService fileTransferService;
  private ExecutorService executorService;
  private DatagramSocket socket;
  private boolean terminate = false;
  private HashMap<Node, List<Executor>> pendingExecutors;
  private Logger logger;

  public QueryListener(AbstractFileTransferService fileTransferService, int port)
      throws SocketException {
    this.fileTransferService = fileTransferService;
    executorService = Executors.newCachedThreadPool();
    pendingExecutors = new HashMap<>();
    socket = new DatagramSocket(null);
    socket.setReuseAddress(true);
    socket.bind(new InetSocketAddress(port));
    socket.setSoTimeout(1000);
    logger = Logger.getLogger(this.getClass().getName());
  }

  public DatagramSocket getSocket() {
    return socket;
  }

  @Override
  public void run() {
    while (!terminate) {
      byte[] buffer = new byte[65536];
      DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
      try {
        socket.receive(incoming);
        String message = new String(buffer).split("\0")[0];
        Node origin = new Node(incoming.getAddress(), incoming.getPort());
        executorService.submit(new ListenerThread(message, origin));
      } catch (SocketTimeoutException e) {
        logger.log(Level.FINE, "Listener timeout");
      } catch (IOException e) {
        throw new RuntimeException("IO exception in socket listener");
      }
    }
    while (socket.isBound()) {
      socket.disconnect();
      socket.close();
    }
  }

  /**
   * Used by executors to tell query listener to notify them when a message is received from a node
   *
   * @param node node from which executor is expecting a message
   * @param executor who is expecting the message
   */
  public void registerForResponse(Node node, Executor executor) {
    if (pendingExecutors.containsKey(node)) {
      pendingExecutors.get(node).add(executor);
    } else {
      pendingExecutors.put(node, new LinkedList<>(Collections.singletonList(executor)));
    }
  }

  /**
   * Used by executors to unregister from future responses from a node
   *
   * @param node node to which it previously registered to
   * @param executor who wants to stop notifications
   */
  public void unRegisterForResponse(Node node, Executor executor) {
    pendingExecutors.get(node).remove(executor);
  }

  public void stop() {
    terminate = true;
  }

  private class ListenerThread implements Runnable {
    String message;
    Node origin;
    FileHandler fileHandler;

    public ListenerThread(String message, Node origin) {
      this.message = message;
      this.origin = origin;
      fileHandler = fileTransferService.getFileHandler();
    }

    @Override
    public void run() {
      String queryType = message.split(" ")[1];
      switch (queryType) {
        case "SEROK":
          pendingExecutors
              .get(origin)
              .forEach(
                  (executor) -> {
                    executor.notify(message);
                  });
          String[] data = message.split(" ");
          int numberOfFiles = Integer.parseInt(data[2]);
          if (numberOfFiles > 0) {
            try {
              Node source = new Node(InetAddress.getByName(data[3]), Integer.parseInt(data[4]));
              // todo: check if the file names are correct
              Stream.of(data)
                  .skip(6)
                  .forEach(
                      fileName -> {
                        fileHandler.downloadFileToCache(source, fileName);
                      });
            } catch (UnknownHostException e) {
              logger.log(
                  Level.WARNING,
                  String.format(
                      "Failed to get Inet address ipAddress: %s port: %s", data[3], data[4]));
            }
          }
          break;
        case "REGOK":
        case "JOINOK":
        case "LEAVEOK":
          pendingExecutors
              .get(origin)
              .forEach(
                  (executor) -> {
                    executor.notify(message);
                  });
          break;
        default:
          throw new IllegalStateException("Unexpected value: " + queryType);
      }
    }
  }
}
