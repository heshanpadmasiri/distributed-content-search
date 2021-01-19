package com.distributed.p2pFileTransfer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

class QueryListenerTest {
    AbstractFileTransferService fileTransferService;
    FileHandler fileHandler;
    QueryListener queryListener;
    Thread queryListenerThread;
    final int QUERY_LISTENER_PORT = 7555;
    final int SENDER_PORT = 7556;
    @BeforeEach
    void setUp() throws SocketException {
       fileTransferService = mock(AbstractFileTransferService.class);
       fileHandler = mock(FileHandler.class);
       when(fileTransferService.getFileHandler()).thenReturn(fileHandler);
       queryListener = new QueryListener(fileTransferService, QUERY_LISTENER_PORT);
       queryListenerThread = new Thread(queryListener);
       queryListenerThread.start();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        this.queryListener.stop();
        queryListenerThread.join(10);
    }

    @Test
    void registerForResponse() throws IOException, InterruptedException {
        Executor executor = mock(Executor.class);
        DatagramSocket sender = new DatagramSocket(SENDER_PORT);
        Node senderNode =  new Node(InetAddress.getLoopbackAddress(), SENDER_PORT);
        Node receiver = new Node(InetAddress.getLocalHost(), QUERY_LISTENER_PORT);
        queryListener.registerForResponse(senderNode, executor);
        String message = "0114 SEROK 3 129.82.128.1 2301 baby_go_home.mp3 baby_come_back.mp3 baby.mpeg";
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        DatagramPacket datagramPacket = new DatagramPacket(data, data.length, receiver.getSocketAddress());
        sender.send(datagramPacket);
        TimeUnit.SECONDS.sleep(1);
        verify(executor).notify(message);
        Node fileSource = new Node(InetAddress.getByName("129.82.128.1"), 2301);
        verify(fileHandler).downloadFileToCache(fileSource,"baby_go_home.mp3");
        verify(fileHandler).downloadFileToCache(fileSource,"baby_come_back.mp3");
        verify(fileHandler).downloadFileToCache(fileSource,"baby.mpeg");
    }
}