package com.distributed.p2pFileTransfer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

abstract  public class Executor implements Callable<QueryResult>{
    Query query;
    String message;
    Node destination;
    DatagramSocket socket;

    public Executor(Query query, DatagramSocket socket) {
        this.query = query;
        this.socket = socket;
        this.message = query.body;
        this.destination = query.destination;
    }
}

class SearchQueryExecutor  extends Executor {

    public SearchQueryExecutor(Query query, DatagramSocket socket) {
        super(query, socket);
    }

    @Override
    public QueryResult call() throws Exception {
        byte[] data = query.body.getBytes(StandardCharsets.UTF_8);
        DatagramPacket sendDatagram =
                new DatagramPacket(data, data.length, query.destination.getSocketAddress());
        try {
            socket.send(sendDatagram);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }
}
