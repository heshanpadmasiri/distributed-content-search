package com.distributed.p2pFileTransfer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketListener implements Runnable {
    private String expectedMessage;
    private String response;
    private DatagramSocket socket;
    private boolean terminate = false;
    Node node;
    private Logger logger;

    public SocketListener(int port, String expectedMessage, String response)
            throws SocketException {
        socket = new DatagramSocket(port);
        socket.setSoTimeout(1000);
        node = new Node(socket.getInetAddress(), port);
        this.expectedMessage = expectedMessage;
        this.response = response;
        logger = Logger.getLogger(this.getClass().getName());
    }
    public SocketListener(int port, String expectedMessage)
            throws SocketException {
        socket = new DatagramSocket(port);
        socket.setSoTimeout(1000);
        node = new Node(socket.getInetAddress(), port);
        this.expectedMessage = expectedMessage;
        logger = Logger.getLogger(this.getClass().getName());
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
                    if(response != null){
                        byte[] responseData = response.getBytes(StandardCharsets.UTF_8);
                        DatagramPacket responseDatagram =
                                new DatagramPacket(
                                        responseData, responseData.length, incoming.getAddress(), incoming.getPort());
                        socket.send(responseDatagram);
                    }
                    this.stop();
                } else {
                    throw new RuntimeException(String.format("Invalid message received : %s", message));
                }
                this.terminate = true;
            } catch (SocketTimeoutException e) {
                logger.log(Level.INFO,"Listener timeout");
            } catch (IOException e) {
                throw new RuntimeException("IO exception in socket listener");
            }
        }
        socket.close();
    }

    public void stop() {
        terminate = true;
    }
}
