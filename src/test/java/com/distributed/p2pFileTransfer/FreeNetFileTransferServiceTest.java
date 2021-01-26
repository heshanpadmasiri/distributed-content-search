package com.distributed.p2pFileTransfer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FreeNetFileTransferServiceTest {

  static Thread fileNodeThread;
  static Node fileNodeNode;
  static TestNode fileNode;
  private AbstractFileTransferService fileTransferService;
  private static Path cache_dir;
  private static Path local_dir;
  private static Properties config;


  @Test
  void searchForExistingFile() {
    Future<List<String>> queryResultFuture = fileTransferService.searchForFile("Lord of the rings");
    try {
      List<String> result = queryResultFuture.get();
      assertNotNull(result);
      assertEquals(result.size(), 3);
      assertTrue(result.contains("Lord of the rings"));
      assertTrue(result.contains("abLord of the rings"));
      assertTrue(result.contains("Lord of the ringsab"));
      assertFalse(result.contains("testFile"));
    } catch (InterruptedException | ExecutionException e) {
      assertNull(e);
    }
  }

  @Test
  void searchForNonExistingFile() {
    Future<List<String>> queryResultFuture = fileTransferService.searchForFile("Non existing file");
    try {
      List<String> result = queryResultFuture.get();
      assertNotNull(result);
      assertEquals(result.size(), 0);
    } catch (InterruptedException | ExecutionException e) {
      assertNull(e);
    }
  }

  @Test
  void downloadExistingFileFile() {}

  @Test
  void downloadFileFrom() {}

  @BeforeEach
  void setUp() throws SocketException, UnknownHostException {
    Network networkMock = mock(Network.class);
    when(networkMock.getNeighbours()).thenReturn(new Iterator<Node>() {
      int remaining = 1;
      @Override
      public boolean hasNext() {
        return remaining > 0;
      }

      @Override
      public Node next() {
        remaining--;
        return fileNodeNode;
      }
    });
    fileTransferService = FreeNetFileTransferService.getInstance(config, networkMock);
  }


  @BeforeAll
  static void beforeAll() {
    List<String> files =
        Stream.of("Lord of the rings", "abLord of the rings", "Lord of the ringsab", "testFile")
            .collect(Collectors.toList());
    fileNode = new FileNode(1200, files);
    fileNodeNode = fileNode.getNode();
    fileNodeThread = new Thread(fileNode);
    fileNodeThread.start();

    Path currentRelativePath = Paths.get("");
    cache_dir = Paths.get(currentRelativePath.toString(), "cache");
    local_dir = Paths.get(currentRelativePath.toString(), "local");
    cache_dir.toFile().mkdir();
    local_dir.toFile().mkdir();
    config = new Properties();
    config.setProperty("cache_dir", cache_dir.toString());
    config.setProperty("local_dir", local_dir.toString());
    config.setProperty("cache_size", "15");
    config.setProperty("port", "1234");
    config.setProperty("server_port", "4321");
  }

  private static void deleteDir(File dir) {
    File[] contents = dir.listFiles();
    if (contents != null) {
      for (File f : contents) {
        deleteDir(f);
      }
    }
    dir.delete();
  }

  @AfterAll
  static void afterAll() throws InterruptedException {
    deleteDir(cache_dir.toFile());
    deleteDir(local_dir.toFile());
    fileNode.setTerminate(true);
    fileNodeThread.join();
  }

  private static class FileNode extends TestNode {
    List<String> filesInNode;
    Pattern fileNamePattern;
    CommandBuilder commandBuilder;

    public FileNode(int port, List<String> filesInNode) {
      super(port);
      this.filesInNode = filesInNode;
      fileNamePattern = Pattern.compile(".*\"(.*)\".*");
      commandBuilder = CommandBuilder.getInstance(getNode());
    }

    @Override
    protected String getResponse(String received) {
      Matcher matcher = fileNamePattern.matcher(received);
      String targetQuery = "<None>";
      if (matcher.find()){
        targetQuery = matcher.group(1);
      }
      String[] data = received.split(" ");
      UUID id = UUID.fromString(data[data.length-1]);
      String finalTargetQuery = targetQuery;
      List<String> matchingFiles =
          filesInNode.stream()
              .filter(
                  fileName -> Pattern.matches(String.format(".*%s.*", finalTargetQuery), fileName))
              .collect(Collectors.toList());
      String message = commandBuilder.getSearchOkCommand(matchingFiles, id);
      return message;
    }
  }
}
