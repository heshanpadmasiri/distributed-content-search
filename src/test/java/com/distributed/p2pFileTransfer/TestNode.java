package com.distributed.p2pFileTransfer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public abstract class TestNode implements Runnable {

  private DatagramSocket socket;
  private boolean terminate = false;
  private Node node;

  public TestNode(int port) {
    try {
      socket = new DatagramSocket(port);
      socket.setSoTimeout(10);
    } catch (SocketException e) {
      e.printStackTrace();
    }
    node = new Node(InetAddress.getLoopbackAddress(), port);
  }

  public Node getNode() {
    return node;
  }

  public void setTerminate(boolean terminate) {
    this.terminate = terminate;
  }

  protected abstract String getResponse(String received);

  @Override
  public void run() {
    while (!terminate) {
      byte[] buffer = new byte[65536];
      DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
      try {
        socket.receive(incoming);
        String received = new String(buffer).split("\0")[0];
        String response = getResponse(received);
        byte[] responseData = response.getBytes(StandardCharsets.UTF_8);
        DatagramPacket responseDatagram =
            new DatagramPacket(
                responseData, responseData.length, incoming.getAddress(), incoming.getPort());
        socket.send(responseDatagram);
      } catch (SocketTimeoutException ignored) {
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    socket.disconnect();
    socket.close();
  }
}
