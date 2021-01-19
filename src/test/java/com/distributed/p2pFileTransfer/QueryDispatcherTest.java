package com.distributed.p2pFileTransfer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QueryDispatcherTest {

  SocketListener socketListener;
  QueryDispatcher queryDispatcher;
  AbstractFileTransferService fileTransferService;
  Thread socketThread;
  QueryListener queryListener;
  private int socketListenerPort = 5555;

  private class SocketListener implements Runnable {
    private String lastMessage = "<None>";
    private DatagramSocket socket;
    private boolean terminate = false;
    private Node node;
    private int messageCount = 0;

    public SocketListener(int port) throws SocketException {
      try {
        socket = new DatagramSocket(port);
      } catch (BindException ex) {
        socket = new DatagramSocket();
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(port));
      }
      socket.setSoTimeout(10);

      node = new Node(socket.getInetAddress(), port);
    }

    @Override
    public void run() {
      while (!terminate) {
        byte[] buffer = new byte[65536];
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        try {
          socket.receive(incoming);
          synchronized (lastMessage) {
            lastMessage = new String(buffer).split("\0")[0];
            messageCount++;
          }
        } catch (SocketTimeoutException ignored) {
        } catch (IOException e) {
          throw new RuntimeException("IO exception in socket listener");
        }
      }
      socket.disconnect();
      socket.close();
    }

    public String getLastMessage() {
      String message;
      synchronized (lastMessage) {
        message = lastMessage;
      }
      return message;
    }

    public int getMessageCount() {
      int count;
      synchronized (lastMessage) {
        count = messageCount;
      }
      return count;
    }

    public void stop() {
      terminate = true;
    }

    public Node toNode() {
      return node;
    }
  }

  @BeforeEach
  void setUp() throws SocketException {
    try {
      socketListener = new SocketListener(socketListenerPort);
      socketListenerPort += 10;
      socketThread = new Thread(socketListener);
      socketThread.start();
    } catch (SocketException e) {
      throw new RuntimeException("Failed to start the socket listener");
    }
    fileTransferService = mock(AbstractFileTransferService.class);
    queryListener = new QueryListener(fileTransferService, 5556);
    when(fileTransferService.getQueryListener()).thenReturn(queryListener);
    try {
      queryDispatcher = new QueryDispatcher(fileTransferService, 5556);
    } catch (SocketException e) {
      throw new RuntimeException("Failed to start query dispatcher");
    }
  }

  @AfterEach
  void tearDown() {
    this.socketListener.stop();
    try {
      socketThread.join();
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupt exception");
    }
    queryListener.stop();
  }

  @Test
  void dispatchOne() {
    String[] messages = {
      "0047 SER 129.82.62.142 5070 \"Lord of the rings\"",
      "0027 JOIN 64.12.123.190 432",
      "0028 LEAVE 64.12.123.190 432",
    };
    for (String message : messages) {
      Query query = Query.createQuery(message, socketListener.toNode());
      this.queryDispatcher.dispatchOne(query);
      try {
        TimeUnit.SECONDS.sleep(1);
        String last = socketListener.getLastMessage();
        assertNotNull(last);
        assertEquals(message, last);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Test
  void dispatchAllSearch() {
    List<String> messages =
        Stream.of(
                "0047 SER 129.82.62.142 5070 \"Lord of the rings1\"",
                "0047 SER 129.82.62.142 5070 \"Lord of the rings2\"",
                "0047 SER 129.82.62.142 5070 \"Lord of the rings3\"")
            .collect(Collectors.toList());
    List<Query> queries = Query.createQuery(messages, socketListener.toNode());
    queryDispatcher.dispatchAll(queries);
    try {
      TimeUnit.SECONDS.sleep(1);
      String last = socketListener.getLastMessage();
      int count = socketListener.getMessageCount();
      assertNotNull(last);
      assertTrue(messages.contains(last));
      assertEquals(queries.size(), count);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Test
  void dispatchAnySearch() {

    List<String> messages =
        IntStream.range(1, 100)
            .mapToObj(
                idx -> String.format("0047 SER 129.82.62.142 5070 \"Lord of the rings%d\"", idx))
            .collect(Collectors.toList());
    List<Query> queries = Query.createQuery(messages, socketListener.toNode());
    queryDispatcher.dispatchAny(queries);
    try {
      TimeUnit.SECONDS.sleep(1);
      String last = socketListener.getLastMessage();
      int count = socketListener.getMessageCount();
      assertNotNull(last);
      assertTrue(messages.contains(last));
      assertTrue(count <= queries.size() && count > 0);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
