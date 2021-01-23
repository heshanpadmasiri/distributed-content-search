package com.distributed.p2pFileTransfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CommandBuilderTest {
  CommandBuilder commandBuilder;
  Node currentNode;

  @Test
  void getSearchCommand() {
    List<String> fileNames =
        Stream.of("fileName1", "fileNameA2", "fileNameAAAB3").collect(Collectors.toList());
    for (String fileName : fileNames) {
      String command = commandBuilder.getSearchCommand(fileName);
      String[] data = command.split(" ");
      int length = Integer.parseInt(data[0]);
      String c = data[1];
      String ip = data[2];
      int port = Integer.parseInt(data[3]);
      String file = data[4];
      int hops = Integer.parseInt(data[5]);
      assertEquals(length, command.length());
      assertEquals(c, "SER");
      assertEquals(ip, currentNode.getIpAddress().toString().split("/")[1]);
      assertEquals(port, currentNode.getPort());
      assertEquals(file, String.format("\"%s\"", fileName));
      assertEquals(hops, -1);
    }
  }

  @Test
  void getSearchOkCommand() {}

  @Test
  void getJoinCommand() {}

  @Test
  void getLeaveCommand() {}

  @BeforeEach
  void setUp() {
    currentNode = new Node(InetAddress.getLoopbackAddress(), 5555);
    commandBuilder = CommandBuilder.getInstance(currentNode);
  }
}
