package com.distributed.p2pFileTransfer;

import java.net.*;
import java.util.Objects;

public class Node {

    private InetAddress ipAddress = null;
    private final int port;
    private final InetSocketAddress socketAddress;

    /**
     * Represent a node in the distributed system
     *
     * @param ipAddress IP address of the node
     * @param port      port of the node
     */
    public Node(InetAddress ipAddress, int port) {
        try {
            if (ipAddress.equals(InetAddress.getByName("127.0.1.1"))){
                this.ipAddress = InetAddress.getByName("127.0.0.1");
            } else {
                this.ipAddress = ipAddress;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.port = port;
        socketAddress = new InetSocketAddress(this.ipAddress, port);
    }

    public Node(int port) throws UnknownHostException {
        this(InetAddress.getLocalHost(), port);
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return port == node.port && Objects.equals(ipAddress, node.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddress, port);
    }

    @Override
    public String toString() {
        return "Node{" +
                "ipAddress=" + ipAddress +
                ", port=" + port +
                '}';
    }
}
