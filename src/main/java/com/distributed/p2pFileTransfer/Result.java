package com.distributed.p2pFileTransfer;

import java.util.UUID;

public abstract class Result {
    final String body;
    final UUID id;
    final int state;

    /**
     * Used to represent a result within the system. Never use directly but
     * extend to represent specific result types
     * @param body representing the result
     * @param state representing the state of result. 0 represent success all other values represent an error
     */
    public Result(String body, int state) {
        this.body = body;
        this.id = UUID.randomUUID();
        this.state = state;
    }
}
