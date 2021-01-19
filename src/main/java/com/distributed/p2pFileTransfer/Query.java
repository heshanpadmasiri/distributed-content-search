package com.distributed.p2pFileTransfer;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Query {
    final String body;
    final UUID id;
    final Node destination;

    /**
     * Used to represent Queries within the system. All fields are final to prevent
     * modifications after creation. Each instance have a UUID that can be used to
     * identify it.
     *
     * @param body        query as given in the problem definition
     * @param destination node representing the destination of query
     */
    private Query(String body, Node destination) {
        this.body = body;
        this.id = UUID.randomUUID();
        this.destination = destination;
    }

    /**
     * Create a query
     *
     * @param body        string according to the problem description
     * @param destination destination node
     * @return query
     */
    public static Query createQuery(String body, Node destination) {
        return new Query(body, destination);
    }

    /**
     * Create copies of same query to multiple destinations
     *
     * @param body         string according to the problem description
     * @param destinations destination nodes
     * @return list of queries
     */
    public static List<Query> createQuery(String body, List<Node> destinations) {
        return destinations.stream().map(destination -> new Query(body, destination)).collect(Collectors.toList());
    }

    /**
     * Create multiple queries to the same destination
     *
     * @param bodies      list of strings according to the problem description
     * @param destination destination node
     * @return list of queries
     */
    public static List<Query> createQuery(List<String> bodies, Node destination) {
        return bodies.stream().map(body -> new Query(body, destination)).collect(Collectors.toList());
    }

    /**
     * Create multiple queries to multiple destinations
     *
     * @param bodies       list of strings according to the problem description
     * @param destinations destination nodes
     * @return list of queries
     */
    public static List<Query> createQuery(List<String> bodies, List<Node> destinations) {
        assert bodies.size() == destinations.size();
        return IntStream.range(0, bodies.size()).mapToObj(i -> new Query(bodies.get(i), destinations.get(i)))
                .collect(Collectors.toList());
    }
}

