package com.distributed.p2pFileTransfer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class MainTests {

  static String clientCacheDir = "/home/kalana/distributed/content/node/cache_storage";
  static String clientLocalDir = "/home/kalana/distributed/content/node/local_storage";
  long cacheSize = 10000000;
  String clientPort = "9000";

  static String serverCacheDir = "/home/kalana/distributed/content/server/cache_storage";
  static String serverLocalDir = "/home/kalana/distributed/content/server/local_storage";
  String serverPort = "7000";

  static Path clientPath, serverPath;

  @BeforeEach
  public void setUp() {
    FileHandler server = new FileHandler(serverCacheDir, serverLocalDir, cacheSize, serverPort);
  }

  /**
   * Download file from a node
   *
   * @throws UnknownHostException
   */
  @Test
  void testFileDownload() throws UnknownHostException, InterruptedException {

    FileHandler fHandler = new FileHandler(clientCacheDir, clientLocalDir, cacheSize, clientPort);
    InetAddress add = InetAddress.getByName("127.0.0.1");
    Node source = new Node(add, Integer.parseInt(serverPort));
    fHandler.downloadFileToLocal(source, "sites.csv");
    // fHandler.downloadFileToCache(source, "sites.csv");
    // Sleep is added to wait till the file download is completed before exiting
    Thread.sleep(2000);
  }

  /** Search for file in self storage */
  @Test
  void testFileSearch() {
    FileHandler fHandler = new FileHandler(clientCacheDir, clientLocalDir, cacheSize, clientPort);
    List<String> matchingFiles = fHandler.searchForFile("s");
    matchingFiles.forEach(System.out::println);
  }

  /** Create required space in the cache storage */
  @Test
  void testCacheSpace() {
    FileHandler fHandler = new FileHandler(clientCacheDir, clientLocalDir, cacheSize, clientPort);
    fHandler.makeCacheSpace(3000000);
  }

  @BeforeAll
  static void beforeAll() throws IOException {
    Path currentRelativePath = Paths.get("");
    clientPath = Paths.get(currentRelativePath.toString(), "node");
    serverPath = Paths.get(currentRelativePath.toString(), "server");
    clientPath.toFile().mkdir();
    serverPath.toFile().mkdir();

    Path clientCachePath = Paths.get(currentRelativePath.toString(), "node", "cache");
    Path clientLocalPath = Paths.get(currentRelativePath.toString(), "node", "local");
    Path serverCachePath = Paths.get(currentRelativePath.toString(), "server", "cache");
    Path serverLocalPath = Paths.get(currentRelativePath.toString(), "server", "local");
    Path serverDummyFile = Paths.get(serverLocalPath.toString(), "sites.csv");
    clientCacheDir = clientCachePath.toAbsolutePath().toString();
    clientLocalDir = clientLocalPath.toAbsolutePath().toString();
    serverCacheDir = serverCachePath.toAbsolutePath().toString();
    serverLocalDir = serverLocalPath.toAbsolutePath().toString();
    clientCachePath.toFile().mkdir();
    clientLocalPath.toFile().mkdir();
    serverCachePath.toFile().mkdir();
    serverLocalPath.toFile().mkdir();
    serverDummyFile.toFile().createNewFile();
  }

  @AfterAll
  static void afterAll() {
    deleteDir(clientPath.toFile());
    deleteDir(serverPath.toFile());
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
}
