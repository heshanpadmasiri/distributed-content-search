package com.distributed.p2pFileTransfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
      String hops = data[5];
      assertEquals(length, command.length());
      assertEquals(c, "SER");
      assertEquals(ip, currentNode.getIpAddress().toString().split("/")[1]);
      assertEquals(port, currentNode.getPort());
      assertEquals(file, String.format("\"%s\"", fileName));
      assertEquals(hops, "<id>");
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
    String hops = data[5];
    List<String> res_files = IntStream.range(6, data.length).mapToObj(idx -> data[idx]).collect(Collectors.toList());
    assertEquals(length, response.length());
    assertEquals(command, "SEROK");
    assertEquals(ip, currentNode.getIpAddress().toString().split("/")[1]);
    assertEquals(port, currentNode.getPort());
    assertEquals(file_count, res_files.size());
    assertEquals(res_files, fileNames);
    assertEquals(hops, "<id>");
  }

  @Test
  void getJoinCommand() {
    String response = commandBuilder.getJoinCommand();
    String[] data = response.split(" ");
    int length = Integer.parseInt(data[0]);
    String command = data[1];
    String ip = data[2];
    int port = Integer.parseInt(data[3]);
    assertEquals(length, response.length());
    assertEquals(command, "JOIN");
    assertEquals(ip, currentNode.getIpAddress().toString().split("/")[1]);
    assertEquals(port, currentNode.getPort());
  }

  @Test
  void getLeaveCommand() {
    String response = commandBuilder.getLeaveCommand();
    String[] data = response.split(" ");
    int length = Integer.parseInt(data[0]);
    String command = data[1];
    String ip = data[2];
    int port = Integer.parseInt(data[3]);
    assertEquals(length, response.length());
    assertEquals(command, "LEAVE");
    assertEquals(ip, currentNode.getIpAddress().toString().split("/")[1]);
    assertEquals(port, currentNode.getPort());
  }

  @Test
  void getJoinOkCommand() {
    String message = commandBuilder.getJoinOkCommand();
    String[] data = message.split(" ");
    int length = Integer.parseInt(data[0]);
    String command = data[1];
    int value = Integer.parseInt(data[2]);
    assertEquals(length, message.length());
    assertEquals(command, "JOINOK");
    assertEquals(value, 0);
  }

  @Test
  void getLeaveOkCommand() {
    String message = commandBuilder.getLeaveOkCommand();
    String[] data = message.split(" ");
    int length = Integer.parseInt(data[0]);
    String command = data[1];
    int value = Integer.parseInt(data[2]);
    assertEquals(length, message.length());
    assertEquals(command, "LEAVEOK");
    assertEquals(value, 0);
  }

  @Test
  void getRegisterCommand() {
    String message = commandBuilder.getRegisterCommand("user");
    String[] data = message.split(" ");
    int length = Integer.parseInt(data[0]);
    String command = data[1];
    String ip = data[2];
    int port = Integer.parseInt(data[3]);
    String userName = data[4];
    assertEquals(length, message.length());
    assertEquals(command, "REG");
    assertEquals(ip, currentNode.getIpAddress().toString().split("/")[1]);
    assertEquals(port, currentNode.getPort());
    assertEquals(userName, "user");
  }

  @Test
  void getUnRegisterCommand() {
    String message = commandBuilder.getUnRegisterCommand("user");
    String[] data = message.split(" ");
    int length = Integer.parseInt(data[0]);
    String command = data[1];
    String ip = data[2];
    int port = Integer.parseInt(data[3]);
    String userName = data[4];
    assertEquals(length, message.length());
    assertEquals(command, "UNREG");
    assertEquals(ip, currentNode.getIpAddress().toString().split("/")[1]);
    assertEquals(port, currentNode.getPort());
    assertEquals(userName, "user");
  }

  @BeforeEach
  void setUp() throws UnknownHostException {
    currentNode = new Node(InetAddress.getLocalHost(), 5555);
    commandBuilder = CommandBuilder.getInstance(currentNode);
  }
}
