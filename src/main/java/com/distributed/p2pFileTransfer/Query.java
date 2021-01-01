package com.distributed.p2pFileTransfer;

import java.util.UUID;

//todo move to a factory construction to allow broad cast creation
public class Query {
    final String body;
    final UUID id;
    final Node destination;

    /**
     * Used to represent Queries within the system. All fields are final to prevent
     * modifications after creation. Each instance have a UUID that can be used to
     * identify it.
     * @param body query as given in the problem definition
     * @param destination node representing the destination of query
     */
    public Query(String body, Node destination) {
        this.body = body;
        this.id = UUID.randomUUID();
        this.destination = destination;
    }
}
