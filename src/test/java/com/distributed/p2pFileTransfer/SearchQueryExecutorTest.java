package com.distributed.p2pFileTransfer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchQueryExecutorTest {
  final int LISTENER_PORT = 5555;
  final int SENDER_PORT = 5556;
  AbstractFileTransferService fileTransferService;
  QueryListener queryListener;
  Thread queryListenerThread;
  ExecutorService executorService;

  @BeforeEach
  void setUp() throws SocketException {
    fileTransferService = mock(AbstractFileTransferService.class);
    queryListener = new QueryListener(fileTransferService, SENDER_PORT);
    when(fileTransferService.getQueryListener()).thenReturn(queryListener);
    queryListenerThread = new Thread(queryListener);
    queryListenerThread.start();
    executorService = Executors.newCachedThreadPool();
  }

  @AfterEach
  void tearDown() throws InterruptedException {
    this.queryListener.stop();
    queryListenerThread.join();
  }

  @Test
  void checkQueryDispatch()
      throws UnknownHostException, SocketException, ExecutionException, InterruptedException {

    String message = "0047 SER 129.82.62.142 5070 \"Lord of the rings\"";
    String expectedResponse = "0114 SEROK 3 129.82.128.1 2301 baby_go_home.mp3 baby_come_back.mp3 baby.mpeg";
    SocketListener listener = new SocketListener(LISTENER_PORT, message, expectedResponse);
    Thread listenerThread  = new Thread(listener);
    listenerThread.start();

    Node senderNode = new Node(InetAddress.getLoopbackAddress(), SENDER_PORT);
    DatagramSocket sender = queryListener.getSocket();
    Node receiver = new Node(InetAddress.getLoopbackAddress(), LISTENER_PORT);
    Query query = Query.createQuery(message, receiver);
    SearchQueryExecutor searchQueryExecutor = new SearchQueryExecutor(query, sender, queryListener);
    Future<QueryResult> queryResultFuture = executorService.submit(searchQueryExecutor);
    QueryResult result = queryResultFuture.get();
    assertEquals(result.state, 0);
    assertEquals(expectedResponse, result.body);
    assertEquals(result.query, query);

    listenerThread.join();
  }

  private class SocketListener implements Runnable {
    private String expectedMessage;
    private String response;
    private DatagramSocket socket;
    private boolean terminate = false;
    Node node;

    public SocketListener(int port, String expectedMessage, String response)
        throws SocketException {
      socket = new DatagramSocket(port);
      node = new Node(socket.getInetAddress(), port);
      this.expectedMessage = expectedMessage;
      this.response = response;
    }

    @Override
    public void run() {
      while (!terminate) {
        byte[] buffer = new byte[65536];
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        try {
          socket.receive(incoming);
          String message = new String(buffer).split("\0")[0];
          if (message.equals(expectedMessage)) {
            byte[] responseData = response.getBytes(StandardCharsets.UTF_8);
            DatagramPacket responseDatagram =
                new DatagramPacket(
                    responseData, responseData.length, incoming.getAddress(), incoming.getPort());
            socket.send(responseDatagram);
            this.stop();
          } else {
            throw new RuntimeException(String.format("Invalid message received : %s", message));
          }
          this.terminate = true;
        } catch (IOException e) {
          throw new RuntimeException("IO exception in socket listener");
        }
      }
    }

    public void stop() {
      terminate = true;
    }
  }
}
