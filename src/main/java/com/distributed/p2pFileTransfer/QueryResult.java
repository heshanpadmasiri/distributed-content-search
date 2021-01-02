package com.distributed.p2pFileTransfer;

public class QueryResult extends Result{
    final Query query;

    /**
     * Used to represent result of a query
     * @param body response message as given in the problem definition
     * @param state state 0 represent success other values represent failures
     * @param query reference to the query that generated this result
     */
    public QueryResult(String body, int state, Query query) {
        super(body, state);
        this.query = query;
    }
}
