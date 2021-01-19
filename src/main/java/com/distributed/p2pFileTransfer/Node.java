package com.distributed.p2pFileTransfer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;

public class Node {

    private final InetAddress ipAddress;
    private final int port;
    private final InetSocketAddress socketAddress;

    /**
     * Represent a node in the distributed system
     *
     * @param ipAddress IP address of the node
     * @param port      port of the node
     */
    public Node(InetAddress ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
        socketAddress = new InetSocketAddress(this.ipAddress, port);
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
}
