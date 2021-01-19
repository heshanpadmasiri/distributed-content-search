package com.distributed.p2pFileTransfer;


//todo: make this thread safe
class QueryQueue<T> {

    /**
     * Used to check if a query already pending in the queue
     *
     * @param query
     * @return
     */
    boolean isInQueue(T query){
        throw new RuntimeException("not implemented");
    }
}
