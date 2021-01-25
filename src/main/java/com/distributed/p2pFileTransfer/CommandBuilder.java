package com.distributed.p2pFileTransfer;

import java.util.*;
import java.util.regex.Pattern;

public class CommandBuilder {
  Node currentNode;
  static Map<Node, CommandBuilder> instances = new HashMap<>();

  private CommandBuilder(Node node) {
    currentNode = node;
  }

  /**
   * Get an command builder for a given node. There can be only one instance
   *
   * @param currentNode node for which commands must be built.
   * @return Command builder singleton with current node
   */
  public static CommandBuilder getInstance(Node currentNode) {
    if (instances.containsKey(currentNode)){
      return instances.get(currentNode);
    } else{
      CommandBuilder commandBuilder = new CommandBuilder(currentNode);
      instances.put(currentNode, commandBuilder);
      return commandBuilder;
    }
  }

  /**
   * Get file search string
   *
   * @param fileName name of the file to search
   * @return file search string
   */
  public String getSearchCommand(String fileName) {
    assert ! Pattern.matches(".*<id>.*",fileName);
    String body = String.format(
            "SER %s %d \"%s\" <id>",currentIp(), currentNode.getPort(), fileName);
    return composeWithLength(body);
  }

  private String currentIp() {
    return currentNode.getIpAddress().toString().split("/")[1];
  }

  /**
   * Get file search success string
   *
   * @param files names of the files matching the search query
   * @return file search success string
   */
  public String getSearchOkCommand(List<String> files, UUID queryId) {
    StringBuilder builder =
        new StringBuilder(
            String.format(
                "SEROK %d %s %d %s", files.size(), currentIp(), currentNode.getPort(), queryId));
    for (String file : files) {
      builder.append(String.format(" %s", file));
    }
    String message = builder.toString().trim();
    return composeWithLength(message);
  }

  private String composeWithLength(String body) {
    return String.format("%04d %s", body.length() + 5, body);
  }

  /**
   * Get join string
   *
   * @return join string
   */
  public String getJoinCommand() {
    String body = String.format("JOIN %s %d", currentIp(), currentNode.getPort());
    return composeWithLength(body);
  }

  /**
   * Get join ok message
   *
   * @param value 0 sucess 9999 failure
   * @return join message
   */
  public String getJoinOkCommand(int value) {
    String body = String.format("JOINOK %d", value);
    return composeWithLength(body);
  }

  /**
   * Return join success message. Similar to getJoinOKCommand(0)
   *
   * @return return join success message
   */
  public String getJoinOkCommand() {
    return getJoinOkCommand(0);
  }

  /**
   * Get Leave messages
   *
   * @return Leave messages
   */
  public String getLeaveCommand() {
    String body = String.format("LEAVE %s %d", currentIp(), currentNode.getPort());
    return composeWithLength(body);
  }

  /**
   * Get Leave ok message
   *
   * @param value 0 for success 9999 error
   * @return Leave message
   */
  public String getLeaveOkCommand(int value) {
    String body = String.format("LEAVEOK %d", value);
    return composeWithLength(body);
  }

  /**
   * Get Leave ok message with success value. Similar to getLeaveOkCommand(0)
   *
   * @return Leave ok message
   */
  public String getLeaveOkCommand() {
    return getLeaveOkCommand(0);
  }

  /**
   * Get register message
   *
   * @param userName username of the node
   * @return Register message for the username
   */
  public String getRegisterCommand(String userName) {
    String body = String.format("REG %s %d %s", currentIp(), currentNode.getPort(), userName);
    return composeWithLength(body);
  }

  /**
   * Get unregister message
   *
   * @param userName username of the node
   * @return Unregister message for the username
   */
  public String getUnRegisterCommand(String userName) {
    String body = String.format("UNREG %s %d %s", currentIp(), currentNode.getPort(), userName);
    return composeWithLength(body);
  }
}
