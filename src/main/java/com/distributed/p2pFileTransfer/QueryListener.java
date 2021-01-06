package com.distributed.p2pFileTransfer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class QueryListener implements Runnable {
  private final AbstractFileTransferService fileTransferService;
  private ExecutorService executorService;
  private DatagramSocket socket;
  private boolean terminate = false;
  private HashMap<Node, List<Executor>> pendingExecutors;

  public QueryListener(AbstractFileTransferService fileTransferService, int port) {
    this.fileTransferService = fileTransferService;
    executorService = Executors.newCachedThreadPool();
    pendingExecutors = new HashMap<>();
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
        this.terminate = true;
      } catch (IOException e) {
        throw new RuntimeException("IO exception in socket listener");
      }
    }
  }

  /**
   * Used by executors to tell query listener to notify them when a message is received from a node
   * @param node node from which executor is expecting a message
   * @param executor who is expecting the message
   */
  public void registerForResponse(Node node, Executor executor){
    if (pendingExecutors.containsKey(node)){
      pendingExecutors.get(node).add(executor);
    } else {
        pendingExecutors.put(node, new LinkedList<>(Collections.singletonList(executor)));
    }
  }

  /**
   * Used by executors to unregister from future responses from a node
   * @param node node to which it previously registered to
   * @param executor who wants to stop notifications
   */
  public void unRegisterForResponse(Node node, Executor executor){
    pendingExecutors.get(node).remove(executor);
  }


  public void stop() {
    terminate = true;
  }

  private class ListenerThread implements Runnable{
    String message;
    Node origin;

    public ListenerThread(String message, Node origin) {
      this.message = message;
      this.origin = origin;
    }

    @Override
    public void run() {
      String queryType = message.split(" ")[1];
      switch (queryType){
        case "SEROK":
         pendingExecutors.get(origin).forEach((executor) -> {
           executor.notify(message);
         });
          break;
        default:
          throw new IllegalStateException("Unexpected value: " + queryType);
      }
    }
  }
}

