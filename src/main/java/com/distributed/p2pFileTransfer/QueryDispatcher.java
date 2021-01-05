package com.distributed.p2pFileTransfer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Future;

class QueryDispatcher {
  private final AbstractFileTransferService fileTransferService;
  private DatagramSocket socket;
  //    private final QueryQueue;

  QueryDispatcher(AbstractFileTransferService fileTransferService, int port)
      throws SocketException {
    this.fileTransferService = fileTransferService;
    socket = new DatagramSocket(port);
  }

  /**
   * Used to dispatch a query to a single node
   *
   * @param query query to dispatch
   * @return result of query
   */
  Future<QueryResult> dispatchOne(Query query) {
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

  /**
   * Dispatch all the queries given and get the results for all of them
   *
   * @param queries queries to dispatch
   * @return results of all the queries
   */
  List<Future<QueryResult>> dispatchAll(List<Query> queries) {
    return null;
  }

  /**
   * Used to dispatch one of a given set of queries. May dispatch all or a selected number. Use when
   * dispatching any one of the given queries is sufficient.
   *
   * @param queries queries to dispatch
   * @return result of any successful queries if any, else failure
   */
  Future<QueryResult> dispatchAny(List<Query> queries) {
    return null;
  }
}
