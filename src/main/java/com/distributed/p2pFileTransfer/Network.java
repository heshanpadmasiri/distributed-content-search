package com.distributed.p2pFileTransfer;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutionException;

class Network {

    private final AbstractFileTransferService fileTransferService;
    private final Node boostrapServer;

//    private TreeMap<Integer, ArrayList<Node>> treeMap;
//    private Map routingTable = Collections.synchronizedMap(treeMap);

    private TreeMap<Integer, ArrayList<Node>> routingTable =
            new TreeMap<Integer, ArrayList<Node>>(Collections.reverseOrder()); ;

    private QueryDispatcher queryDispatcher;
    private ResponseHandler responseHandler;

    private Node node; // ?????????????????????????????????????????????????????????????????????????????????
    private final String REG = "REG";
    private final String JOIN = "JOIN";
    private final String LEAVE = "LEAVE";
    private final String UNREG = "UNREG";

    final String USERNAME = "USERNAME";
    /**
     * Representation of nodes view of the network. Concrete implementations must connect with the bootstrap server and
     * set the neighbours. If the boostrap server refused connection constructor must disconnect and reconnect with the
     * boostrap server
     *
     * @param fileTransferService
     * @param boostrapServer
     * @throws NodeNotFoundException If unable to connect with the boostrap server
     */
    public Network(AbstractFileTransferService fileTransferService, Node boostrapServer) throws SocketException {
        this.fileTransferService = fileTransferService;
        this.boostrapServer = boostrapServer;

        // register with the BS
        QueryResult response;
        queryDispatcher = new QueryDispatcher(fileTransferService, boostrapServer.getPort());
        while(true) {
            try {
                response = queryDispatcher.dispatchOne(buildRegQuery(node)).get();
                if (response.state == 0) {
                    break;
                }
            }
            catch(ExecutionException | InterruptedException e){
                // e.printStackTrace();
                System.out.println("Error occured");
            }
        }

        // add the neighbour nodes returned by BS to the routing table
        if(response.body != null) {
            responseHandler = new ResponseHandler();
            HashMap<String,String> formattedResponse = responseHandler.handleRegisterResponse(response.body);

            String state = formattedResponse.get("no_nodes");
            if (state.equals("0")||state.equals("1") || state.equals("2")) {
                this.routingTable = new TreeMap<Integer,ArrayList<Node>>();
                routingTable.put(0,new ArrayList<Node>());
                try {
                    if (formattedResponse.get("IP_1") != null) {
                        Node node = new Node(InetAddress.getByName(formattedResponse.get("IP_1")),
                                Integer.parseInt(formattedResponse.get("port_1")));
                        routingTable.get(0).add(node);
                        // join
                    }
                    if (formattedResponse.get("IP_2") != null) {
                        Node node = new Node(InetAddress.getByName(formattedResponse.get("IP_1")),
                                Integer.parseInt(formattedResponse.get("port_2")));
                        addNeighbour(node);
                        routingTable.get(0).add(node);
                        // join
                    }
                } catch (UnknownHostException e) {
                    System.out.println("IP error");
                }

            } else if (state.equals("9999")) {
                System.out.println("Error in command");
            } else if (state.equals("9998")) {
                System.out.println("failed, already registered to you, unregister first");
                // unregister from BS: implement method ?????????????????????????????????????????
            } else if (state.equals("9997")) {
                System.out.println("failed, registered to another user, try a different IP and port");
            } else if (state.equals("9996")) {
                System.out.println("failed, can’t register. BS full");
            }
        }
    }

    /**
     * Get the neighbours of this node.
     *
     * @return iterator of neighbours. Ordering depends on the implementation
     */
    ArrayList<Node> getNeighbours() {
        // have to change the method params returned
        ArrayList<Node> out  = new ArrayList<Node>();

        for(Map.Entry<Integer,ArrayList<Node>> entityArry : routingTable.entrySet()) {
            for(Node entity: entityArry.getValue()) {
                out.add(entity);
            }
        }


        return out;
    }

    /**
     * Used to reset the network state by disconnecting with boostrap server and reconnecting
     *
     * @throws NodeNotFoundException if unable to connect to the boostrap server
     */
    void resetNetwork() throws NodeNotFoundException {
        routingTable.clear();
        return;
    }

    /**
     * Used to add new neighbours to the network
     *
     * @param node new neighbour
     */
    void addNeighbour(Node node) {
        // if the neighbour is found through a search ??????????????????
        // check if the node is already in the routing table
        for(Map.Entry<Integer,ArrayList<Node>> entityArry : routingTable.entrySet()) {
            for(Node entity: entityArry.getValue()) {
                if(node.getPort()==entity.getPort() && node.getIpAddress()==entity.getIpAddress()) {
                    // remove the node from current file count list and add
                    if(routingTable.containsKey(entityArry.getKey()+1)) {
                        routingTable.get(entityArry.getKey()+1).add(entity);
                    }
                    else {
                        // if key file count doesn't exist
                        ArrayList<Node> temp = new ArrayList<Node>();
                        temp.add(entity);
                        routingTable.put(entityArry.getKey() + 1, temp);
                    }
                    // remove
                    entityArry.getValue().remove(entity);
                }
            }
        }
    }



    Query buildRegQuery(Node node) {
        StringBuilder sb = new StringBuilder(" ");
        sb.append(REG);
        sb.append(" ");
        sb.append(node.getIpAddress());
        sb.append(" ");
        sb.append(USERNAME);

        int length = sb.length();
        StringBuilder sb1 = new StringBuilder();
        int count = length;
        while (length%10!=0) {
            sb1.append("0");
            length/=10;
        }
        sb1.append(count);
        sb1.append(sb);

        String body = sb1.toString();
        Query query = Query.createQuery(body,node);

        return query;
    }



}
