package com.distributed.p2pFileTransfer;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

class QueryDispatcher {
  private final AbstractFileTransferService fileTransferService;
  private DatagramSocket socket;
  private ExecutorService executorService;
  //    private final QueryQueue;

  QueryDispatcher(AbstractFileTransferService fileTransferService, int port)
      throws SocketException {
    this.fileTransferService = fileTransferService;
    socket = fileTransferService.getQueryListener().getSocket();
    executorService = Executors.newCachedThreadPool();
  }

  /**
   * Used to dispatch a query to a single node
   *
   * @param query query to dispatch
   * @return result of query
   */
  Future<QueryResult> dispatchOne(Query query) {
    Executor executor;
    String queryType = query.body.split(" ")[1];
    switch (queryType) {
      case "SER":
        executor =
            new FileSearchQueryExecutor(
                query,
                socket,
                fileTransferService.getQueryListener(),
                fileTransferService.getFileHandler()
                );
        break;
      case "UNREG":
      case "JOIN":
      case "LEAVE":
      case "REG":
        executor =
            new AcknowledgedQueryExecutor(query, socket, fileTransferService.getQueryListener());
        break;
      case "SEROK":
      case "UNREGOK":
      case "JOINOK":
      case "LEAVEOK":
      case "REGOK":
        executor =
            new UnAcknowledgedQueryExecutor(query, socket, fileTransferService.getQueryListener());
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + queryType);
    }
    return executorService.submit(executor);
  }

  /**
   * Dispatch all the queries given and get the results for all of them
   *
   * @param queries queries to dispatch
   * @return results of all the queries
   */
  List<Future<QueryResult>> dispatchAll(List<Query> queries) {
    return queries.stream().map(this::dispatchOne).collect(Collectors.toList());
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
