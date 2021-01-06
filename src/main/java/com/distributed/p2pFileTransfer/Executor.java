package com.distributed.p2pFileTransfer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public abstract class Executor implements Callable<QueryResult> {
  Query query;
  String message;
  Node destination;
  DatagramSocket socket;
  QueryListener queryListener;

  public Executor(Query query, DatagramSocket socket, QueryListener queryListener) {
    this.query = query;
    this.socket = socket;
    this.message = query.body;
    this.destination = query.destination;
  }

  public abstract void notify(String message);
}

class SearchQueryExecutor extends Executor {

  private Object monitor;
  private String response;
  private boolean responseReceived = false;

  public SearchQueryExecutor(Query query, DatagramSocket socket, QueryListener queryListener) {
    super(query, socket, queryListener);
  }

  @Override
  public void notify(String message) {
    response = message;
    responseReceived = true;
    synchronized (monitor){
      notifyAll();
    }
  }

  @Override
  public QueryResult call(){
    byte[] data = query.body.getBytes(StandardCharsets.UTF_8);
    DatagramPacket sendDatagram =
        new DatagramPacket(data, data.length, query.destination.getSocketAddress());
    queryListener.registerForResponse(query.destination, this);
    try {
      socket.send(sendDatagram);
    } catch (IOException e) {
      return new QueryResult(String.format("Failed to send message due to : %s", e), 1, query);
    }
    while(!responseReceived){
      synchronized (monitor){
        try {
          monitor.wait();
        } catch (InterruptedException e) {
          return new QueryResult(String.format("Failed to send message due to : %s", e), 2, query);
        }
      }
    }
    return new QueryResult(response, 0, query);
  }
}
