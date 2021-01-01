package com.distributed.p2pFileTransfer;

import java.util.Iterator;

abstract class Network {

    private final AbstractFileTransferService fileTransferService;
    private Node boostrapServer;

    /**
     * Representation of nodes view of the network. Concrete implementations must connect with the bootstrap server and
     * set the neighbours. If the boostrap server refused connection constructor must disconnect and reconnect with the
     * boostrap server
     * @param fileTransferService
     * @param boostrapServer
     * @throws NodeNotFoundException If unable to connect with the boostrap server
     */
    public Network(AbstractFileTransferService fileTransferService, Node boostrapServer) throws NodeNotFoundException{
        this.fileTransferService = fileTransferService;
        this.boostrapServer = boostrapServer;
    }

    /**
     * Get the neighbours of this node.
     * @return iterator of neighbours. Ordering depends on the implementation
     */
    abstract Iterator<Node> getNeighbours();

    /**
     * Used to reset the network state by disconnecting with boostrap server and reconnecting
     * @throws NodeNotFoundException if unable to connect to the boostrap server
     */
    abstract void resetNetwork() throws NodeNotFoundException;

    /**
     * Used to add new neighbours to the network
     * @param node new neighbour
     */
    abstract void addNeighbour(Node node);
}
