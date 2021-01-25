package com.distributed.p2pFileTransfer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
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
  Thread socketThread, queryListenerThread;
  QueryListener queryListener;
  CommandBuilder commandBuilder;
  private int socketListenerPort = 5555;

  private class SocketListener implements Runnable {
    private String lastMessage = "<None>";
    private DatagramSocket socket;
    private boolean terminate = false;
    private Node node;
    private int messageCount = 0;
    private Logger logger;

    public SocketListener(int port) throws SocketException {
      try {
        socket = new DatagramSocket(port);
      } catch (BindException ex) {
        socket = new DatagramSocket();
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(port));
      }
      socket.setSoTimeout(10);

      node = new Node(InetAddress.getLoopbackAddress(), port);
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
            UUID id;
            String[] data = lastMessage.split(" ");
            if (data[1].equals("SER")) {
              id = UUID.fromString(data[data.length - 1]);
            } else {
              id = UUID.randomUUID();
            }
            String responseMessage =
                    commandBuilder.getSearchOkCommand(Collections.singletonList("messageReceived"), id);
            byte[] responseData = responseMessage.getBytes(StandardCharsets.UTF_8);
            DatagramPacket responseDatagram =
                    new DatagramPacket(
                            responseData, responseData.length, incoming.getAddress(), incoming.getPort());
            socket.send(responseDatagram);
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
  void setUp() throws SocketException, UnknownHostException {
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
    commandBuilder = CommandBuilder.getInstance(new Node(InetAddress.getLocalHost(), 5556));
    queryListenerThread = new Thread(queryListener);
    queryListenerThread.start();
    when(fileTransferService.getQueryListener()).thenReturn(queryListener);
    try {
      queryDispatcher = new QueryDispatcher(fileTransferService);
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
    try {
      queryListenerThread.join(10);
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupt exception");
    }
  }

  @Test
  void dispatchOne() {
    List<String> messages =
            Stream.of(
                    commandBuilder.getSearchCommand("Lord of the rings"),
                    commandBuilder.getJoinCommand(),
                    commandBuilder.getLeaveCommand())
                    .collect(Collectors.toList());
    for (String message : messages) {
      Query query = Query.createQuery(message, socketListener.toNode());
      Future<QueryResult> response = this.queryDispatcher.dispatchOne(query);
      try {
        QueryResult result = response.get();
        String last = result.getBody();
        assertNotNull(last);
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }
  }

  @Test
  void dispatchAllSearch() {
    List<String> messages =
            Stream.of("Lord of the rings1", "Lord of the rings2", "Lord of the rings3")
                    .map(fileName -> commandBuilder.getSearchCommand(fileName))
                    .collect(Collectors.toList());
    List<Query> queries = Query.createQuery(messages, socketListener.toNode());
    List<Future<QueryResult>> futures = queryDispatcher.dispatchAll(queries);
    List<QueryResult> results =
            futures.stream()
                    .map(
                            each -> {
                              QueryResult result = null;
                              try {
                                result = each.get();
                              } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                              }
                              return result;
                            })
                    .collect(Collectors.toList());
    results.forEach(Assertions::assertNotNull);
    int count = socketListener.getMessageCount();
    assertEquals(queries.size(), count);
  }

  @Test
  void dispatchAnySearch() throws ExecutionException, InterruptedException {
    List<String> messages =
            IntStream.range(1, 100)
                    .mapToObj(idx -> String.format("Lord of the rings%d", idx))
                    .map(fileName -> commandBuilder.getSearchCommand(fileName))
                    .collect(Collectors.toList());
    List<Query> queries = Query.createQuery(messages, socketListener.toNode());
    QueryResult result = queryDispatcher.dispatchAny(queries).get();
    assertNotNull(result);
    String last = socketListener.getLastMessage();
    int count = socketListener.getMessageCount();
    assertNotNull(last);
    assertTrue(count <= queries.size() && count > 0);
  }
}
