package com.distributed.p2pFileTransfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
  void getSearchOkCommand() {
    List<String> fileNames =
            Stream.of("fileName1", "fileNameA2", "fileNameAAAB3").collect(Collectors.toList());
    String response = commandBuilder.getSearchOkCommand(fileNames);
    String[] data = response.split(" ");
    int length = Integer.parseInt(data[0]);
    String command = data[1];
    int file_count = Integer.parseInt(data[2]);
    String ip = data[3];
    int port = Integer.parseInt(data[4]);
    int hops = Integer.parseInt(data[5]);
    List<String> res_files = IntStream.range(6, data.length).mapToObj(idx -> data[idx]).collect(Collectors.toList());
    assertEquals(length, response.length());
    assertEquals(command, "SEROK");
    assertEquals(ip, currentNode.getIpAddress().toString().split("/")[1]);
    assertEquals(port, currentNode.getPort());
    assertEquals(file_count, res_files.size());
    assertEquals(res_files, fileNames);
    assertEquals(hops, -1);
  }

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
