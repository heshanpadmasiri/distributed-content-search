package com.distributed.p2pFileTransfer;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

//todo: make this thread safe
class QueryQueue<T> {

    /**
     * Used to check if a query already pending in the queue
     *
     * @param query
     * @return
     */
    boolean isInQueue(T query) {
        throw new NotImplementedException();
    }
}
