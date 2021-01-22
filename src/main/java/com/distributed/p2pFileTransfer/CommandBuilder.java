package com.distributed.p2pFileTransfer;

import java.util.List;

public class CommandBuilder {
  Node currentNode;
  static CommandBuilder instance;
  private CommandBuilder(Node node){
    currentNode = node;
  }

  public static CommandBuilder getInstance(Node currentNode){
    if(instance == null){
      instance = new CommandBuilder(currentNode);
    } else {
      assert instance.currentNode == currentNode;
    }
    return instance;
  }

  public String getSearchCommand(String fileName) {
    return null;
  }

  public String getSearchOkCommand(List<String> files) {
    return null;
  }

  public String getJoinCommand() {
    return null;
  }

  public String getLeaveCommand() {
    return null;
  }
}
