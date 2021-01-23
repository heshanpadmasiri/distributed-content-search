package com.distributed.p2pFileTransfer;

import java.util.List;

public class CommandBuilder {
  Node currentNode;
  static CommandBuilder instance;

  private CommandBuilder(Node node) {
    currentNode = node;
  }

  public static CommandBuilder getInstance(Node currentNode) {
    if (instance == null) {
      instance = new CommandBuilder(currentNode);
    } else {
      assert instance.currentNode.equals(currentNode);
    }
    return instance;
  }

  public String getSearchCommand(String fileName) {
    int length = fileName.length() + 29;
    return String.format(
        "%04d SER %s %d \"%s\" -1", length, currentIp(), currentNode.getPort(), fileName);
  }

  private String currentIp() {
    return currentNode.getIpAddress().toString().split("/")[1];
  }

  public String getSearchOkCommand(List<String> files) {
    StringBuilder builder =
        new StringBuilder(
            String.format(
                "SEROK %d %s %d %d", files.size(), currentIp(), currentNode.getPort(), -1));
    for (String file : files) {
      builder.append(String.format(" %s", file));
    }
    String message = builder.toString().trim();
    return composeWithLength(message);
  }

  private String composeWithLength(String body) {
    return String.format("%04d %s", body.length() + 5, body);
  }

  public String getJoinCommand() {
    String body = String.format("JOIN %s %d", currentIp(), currentNode.getPort());
    return composeWithLength(body);
  }

  public String getLeaveCommand() {
    String body = String.format("LEAVE %s %d", currentIp(), currentNode.getPort());
    return composeWithLength(body);
  }
}
