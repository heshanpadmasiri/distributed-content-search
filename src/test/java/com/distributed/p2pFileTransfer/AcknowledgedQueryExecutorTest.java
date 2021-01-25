package com.distributed.p2pFileTransfer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AcknowledgedQueryExecutorTest {
  final int LISTENER_PORT = 6555;
  final int SENDER_PORT = 6556;
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
    queryListenerThread.join(10);
  }

  @Test
  void checkQueryDispatch() throws SocketException, ExecutionException, InterruptedException {
    Node senderNode = new Node(InetAddress.getLoopbackAddress(), SENDER_PORT);
    CommandBuilder commandBuilder = CommandBuilder.getInstance(senderNode);
    String message = commandBuilder.getSearchCommand("Lord of the rings");
    DatagramSocket sender = queryListener.getSocket();
    Node receiver = new Node(InetAddress.getLoopbackAddress(), LISTENER_PORT);
    Query query = Query.createQuery(message, receiver);
    String expectedResponse =
        commandBuilder.getSearchOkCommand(
            Stream.of("baby_go_home.mp3", "baby_come_back.mp3", "baby.mpeg")
                .collect(Collectors.toList()),
            query.id);
    SocketListener listener = new SocketListener(LISTENER_PORT, query.body, expectedResponse);
    Thread listenerThread = new Thread(listener);
    listenerThread.start();

    AcknowledgedQueryExecutor searchQueryExecutor =
        new AcknowledgedQueryExecutor(query, sender, queryListener);
    Future<QueryResult> queryResultFuture = executorService.submit(searchQueryExecutor);
    QueryResult result = queryResultFuture.get();
    assertEquals(result.state, 0);
    assertEquals(expectedResponse, result.body);
    assertEquals(result.query, query);
    listenerThread.join(10);
  }
}
