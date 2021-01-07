package com.distributed.p2pFileTransfer;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QueryDispatcherTest {

  SocketListener socketListener;
  QueryDispatcher queryDispatcher;
  AbstractFileTransferService fileTransferService;
  Thread socketThread;
  QueryListener queryListener;

  private class SocketListener implements Runnable {
    private String lastMessage;
    private DatagramSocket socket;
    private boolean terminate = false;
    private Node node;

    public SocketListener(int port) throws SocketException {
      socket = new DatagramSocket(port);
      node = new Node(socket.getInetAddress(), port);
    }

    @Override
    public void run() {
      while (!terminate) {
        byte[] buffer = new byte[65536];
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        try {
          socket.receive(incoming);
          lastMessage = new String(buffer).split("\0")[0];
          this.terminate = true;
        } catch (IOException e) {
          throw new RuntimeException("IO exception in socket listener");
        }
      }
      socket.close();
    }

    public String getLastMessage() {
      return lastMessage;
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
      socketListener = new SocketListener(5555);
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
  }

  @Test
  @Order(1)
  void dispatchOne() {
    String message = "0047 SER 129.82.62.142 5070 \"Lord of the rings\"";
    Query query = Query.createQuery(message, socketListener.toNode());
    this.queryDispatcher.dispatchOne(query);
    try {
      TimeUnit.SECONDS.sleep(5);
      String last = socketListener.getLastMessage();
      assertNotNull(last);
      assertEquals(last, message);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
