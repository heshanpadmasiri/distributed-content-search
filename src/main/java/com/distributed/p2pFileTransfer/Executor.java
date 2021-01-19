package com.distributed.p2pFileTransfer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Executor implements Callable<QueryResult> {
  final Logger logger;
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
    this.queryListener = queryListener;
    logger = Logger.getLogger(UnAcknowledgedQueryExecutor.class.getName());
  }

  public abstract void notify(String message);
}

class AcknowledgedQueryExecutor extends Executor {

  private final Object monitor = new Object();
  private String response;
  private boolean responseReceived = false;

  public AcknowledgedQueryExecutor(
      Query query, DatagramSocket socket, QueryListener queryListener) {
    super(query, socket, queryListener);
  }

  @Override
  public void notify(String message) {
    // todo : check if the message is a response for the message we send
    response = message;
    responseReceived = true;
    logger.log(Level.INFO, String.format("Message received %s", message));
    synchronized (monitor) {
      monitor.notifyAll();
    }
    queryListener.unRegisterForResponse(destination, this);
  }

  @Override
  public QueryResult call() {
    byte[] data = query.body.getBytes(StandardCharsets.UTF_8);
    DatagramPacket sendDatagram =
        new DatagramPacket(data, data.length, query.destination.getSocketAddress());
    queryListener.registerForResponse(query.destination, this);
    try {
      socket.send(sendDatagram);
    } catch (IOException e) {
      return new QueryResult(String.format("Failed to send message due to : %s", e), 1, query);
    }
    while (!responseReceived) {
      synchronized (monitor) {
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

class FileSearchQueryExecutor extends AcknowledgedQueryExecutor {
  FileHandler fileHandler;

  public FileSearchQueryExecutor(
      Query query,
      DatagramSocket socket,
      QueryListener queryListener,
      FileHandler fileHandler
      ) {
    super(query, socket, queryListener);
    this.fileHandler = fileHandler;
  }

  @Override
  public QueryResult call() {
    QueryResult result = super.call();
    return result;
  }
}

class UnAcknowledgedQueryExecutor extends Executor {
  Logger logger;

  public UnAcknowledgedQueryExecutor(
      Query query, DatagramSocket socket, QueryListener queryListener) {
    super(query, socket, queryListener);
  }

  @Override
  public void notify(String message) {
    logger.log(
        Level.WARNING,
        String.format(
            "Unacknowledged query executor message received Message received %s", message));
  }

  @Override
  public QueryResult call() {
    byte[] data = query.body.getBytes(StandardCharsets.UTF_8);
    DatagramPacket sendDatagram =
        new DatagramPacket(data, data.length, query.destination.getSocketAddress());
    try {
      socket.send(sendDatagram);
    } catch (IOException e) {
      return new QueryResult(String.format("Failed to send message due to : %s", e), 1, query);
    }
    return new QueryResult("Message send successfully", 0, query);
  }
}
