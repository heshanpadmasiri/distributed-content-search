package com.distributed.p2pFileTransfer;

import java.net.InetAddress;

public class Node {

    private final InetAddress ipAddress;
    private final int port;

    /**
     * Represent a node in the distributed system
     *
     * @param ipAddress IP address of the node
     * @param port      port of the node
     */
    public Node(InetAddress ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }
}
