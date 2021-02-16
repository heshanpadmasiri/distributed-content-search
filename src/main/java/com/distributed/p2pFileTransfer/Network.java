package com.distributed.p2pFileTransfer;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class Network {

  private final AbstractFileTransferService fileTransferService;
  private final Node boostrapServer;

  private TreeMap<Integer, ArrayList<Node>> routingTable =
      new TreeMap<Integer, ArrayList<Node>>(Collections.reverseOrder());
  ;

  private QueryDispatcher queryDispatcher;
  private ResponseHandler responseHandler;

  private final CommandBuilder cb;

  final String USERNAME = "USERNAME";
  /**
   * Representation of nodes view of the network. Concrete implementations must connect with the
   * bootstrap server and set the neighbours. If the boostrap server refused connection constructor
   * must disconnect and reconnect with the boostrap server
   *
   * @param fileTransferService
   * @param boostrapServer
   */
  public Network(AbstractFileTransferService fileTransferService, Node boostrapServer) throws NodeNotFoundException{
    this.fileTransferService = fileTransferService;
    this.boostrapServer = boostrapServer;
    this.cb = fileTransferService.getCommandBuilder();

    // register with the BS
    QueryResult response = null;
    try {
      queryDispatcher = new QueryDispatcher(fileTransferService);
      while (true) {
        try {
          Query query = Query.createQuery(cb.getRegisterCommand(USERNAME), boostrapServer);
          response = queryDispatcher.dispatchOne(query).get();
          if (response.body != null) {
            responseHandler = new ResponseHandler();
            HashMap<String, String> formattedResponse =
                    responseHandler.handleRegisterResponse(response.body);
            String state = formattedResponse.get("no_nodes");
            if (state.equals("0") || state.equals("1") || state.equals("2")) {
              addInitialNeighbours(formattedResponse);
              break;
            }
            else if (state.equals("9998")) {
              System.out.println("failed, already registered to you, unregister first");
              disconnect();
            } else {
              switch (state) {
                case "9999":
                  System.out.println("Error in command");
                  break;
                case "9997":
                  System.out.println("failed, registered to another user, try a different IP and port");
                  break;
                case "9996":
                  System.out.println("failed, can’t register. BS full");
                  break;
              }
              break;
            }

            }

          } catch (ExecutionException | InterruptedException e) {
          // e.printStackTrace();
          System.out.println("Error occured");
        }
      }
    } catch (SocketException e) {
      System.out.println(e);
    }
  }

  /**
   * Get the neighbours of this node.
   *
   * @return iterator of neighbours. Ordering depends on the implementation
   */
  Iterator<Node> getNeighbours() {
    // have to change the method params returned
    ArrayList<Node> list = new ArrayList<Node>();
    TreeMap<Integer, ArrayList<Node>> arr = new TreeMap<Integer, ArrayList<Node>>();
    arr.putAll(routingTable);
    for (Map.Entry<Integer, ArrayList<Node>> entityArry : arr.entrySet()) {
      list.addAll(entityArry.getValue());
    }

    return list.iterator();
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
    for (Map.Entry<Integer, ArrayList<Node>> entityArry : routingTable.entrySet()) {
      for (Node entity : entityArry.getValue()) {
        if (node.getPort() == entity.getPort() && node.getIpAddress() == entity.getIpAddress()) {
          // remove the node from current file count list and add
          if (routingTable.containsKey(entityArry.getKey() + 1)) {
            routingTable.get(entityArry.getKey() + 1).add(entity);
          } else {
            // if key file count doesn't exist
            ArrayList<Node> temp = new ArrayList<Node>();
            temp.add(entity);
            routingTable.put(entityArry.getKey() + 1, temp);
          }
          // remove
          entityArry.getValue().remove(entity);
        } else {
          if (routingTable.containsKey(1)) {
            routingTable.get(1).add(entity);
          } else {
            // if key file count doesn't exist
            ArrayList<Node> temp = new ArrayList<Node>();
            temp.add(entity);
            routingTable.put(1, temp);
          }
        }
      }
    }
  }

  private void addInitialNeighbours(HashMap<String, String> response) {
        this.routingTable = new TreeMap<>();
        routingTable.put(0, new ArrayList<>());
        try {
          if (response.get("IP_1") != null) {
            Node node = new Node(
                    InetAddress.getByName(response.get("IP_1")),
                    Integer.parseInt(response.get("port_1")));
            routingTable.get(0).add(node);
            sendJoinRequest(node);
          }
          if (response.get("IP_2") != null) {
            Node node = new Node(
                    InetAddress.getByName(response.get("IP_1")),
                    Integer.parseInt(response.get("port_2")));
            addNeighbour(node);
            routingTable.get(0).add(node);
            sendJoinRequest(node);
          }
        } catch (UnknownHostException unknownHostException) {
          System.out.println("IP error");
        }
  }

  /**
   * Used to disconnect form bootstrap
   * @return future to be resolved when disconnect completed
   */
  public Future<QueryResult> disconnect() {
    Query query = Query.createQuery(cb.getUnRegisterCommand(USERNAME), cb.currentNode);

    try {
      QueryResult response = queryDispatcher.dispatchOne(query).get();
      if (response.body != null) {
        responseHandler = new ResponseHandler();
        HashMap<String, String> formattedResponse =
                responseHandler.handleUnRegisterResponse(response.body);
        if(formattedResponse.get("value").equals("0")) {
          return sendLeaveRequest(cb.currentNode);
        }
        else {
          // failure
          System.out.println(
                  "error while unregistering. IP and port may not be in the registry or command is incorrect.");
          return null;
        }
      }
    } catch (InterruptedException | ExecutionException e) {
      System.out.println(
              "error while unregistering. Exception occured\n"+e);
    }
    return null;
  }

  /**
   * Used to show the routing table
   */
  public void printRoutingTable(){
    ArrayList<Node> list;
    TreeMap<Integer, ArrayList<Node>> arr = new TreeMap<>();
    arr.putAll(routingTable);
    for (Map.Entry<Integer, ArrayList<Node>> entityArray : arr.entrySet()) {
      list = entityArray.getValue();
      for(Node node: list) {
        System.out.print("File Count: "+ entityArray.getKey());
        System.out.print("  Node: " + node.getIpAddress()+ ":" + node.getPort());
        System.out.println();
      }
    }
  }

  /**
   * Used to remove none responsive neighbours from the routing table
   */
  public void removeNeighbour(Node node){
    for (Map.Entry<Integer, ArrayList<Node>> entityArry : routingTable.entrySet()) {
      entityArry.getValue().removeIf(entity ->
              node.getPort() == entity.getPort() && node.getIpAddress() == entity.getIpAddress());
    }
  }

  private void sendJoinRequest(Node node) {
    Query query = Query.createQuery(cb.getJoinCommand(), node);
    queryDispatcher.dispatchOne(query);
  }


  private Future<QueryResult> sendLeaveRequest(Node node) {
    Query query = Query.createQuery(cb.getLeaveCommand(), node);

    //      QueryResult response = queryDispatcher.dispatchOne(query);
//      if (response!=null) {
//        responseHandler = new ResponseHandler();
//        HashMap<String, String> formattedResponse =
//                responseHandler.handleLeaveResponse(response.body);
//        formattedResponse.get("val");
//      }
    return queryDispatcher.dispatchOne(query);

  }
}
