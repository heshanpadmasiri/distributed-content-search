package com.distributed.p2pFileTransfer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnAcknowledgedQueryExecutorTest {
  final int LISTENER_PORT = 8555;
  final int SENDER_PORT = 8556;
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
    executorService = Executors.newSingleThreadExecutor();
  }

  @AfterEach
  void tearDown() throws InterruptedException {
    this.queryListener.stop();
    queryListenerThread.join(10);
  }

  @Test
  void checkQueryDispatch() throws SocketException, ExecutionException, InterruptedException {

    String message = "0114 SEROK 3 129.82.128.1 2301 baby_go_home.mp3 baby_come_back.mp3 baby.mpeg";
    SocketListener listener = new SocketListener(LISTENER_PORT, message);
    Thread listenerThread = new Thread(listener);
    listenerThread.start();
    Node senderNode = new Node(InetAddress.getLoopbackAddress(), SENDER_PORT);
    DatagramSocket sender = queryListener.getSocket();
    Node receiver = new Node(InetAddress.getLoopbackAddress(), LISTENER_PORT);
    Query query = Query.createQuery(message, receiver);
    UnAcknowledgedQueryExecutor searchOkQueryExecutor =
        new UnAcknowledgedQueryExecutor(query, sender, queryListener);
    Future<QueryResult> queryResultFuture = executorService.submit(searchOkQueryExecutor);
    QueryResult result = queryResultFuture.get();
    assertEquals(result.body, "Message send successfully");
    assertEquals(result.state, 0);
    assertEquals(result.query, query);
    listener.stop();
  }
}
