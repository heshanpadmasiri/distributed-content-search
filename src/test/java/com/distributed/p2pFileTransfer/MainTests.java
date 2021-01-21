package com.distributed.p2pFileTransfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

class MainTests {
    
    String clientCacheDir = "/home/kalana/distributed/content/node/cache_storage";
    String clientLocalDir = "/home/kalana/distributed/content/node/local_storage";
    long cacheSize = 10000000;
    String clientPort = "9000";

    String serverCacheDir = "/home/kalana/distributed/content/server/cache_storage";
    String serverLocalDir = "/home/kalana/distributed/content/server/local_storage";
    String serverPort = "7000";

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
        //fHandler.downloadFileToCache(source, "sites.csv");
        // Sleep is added to wait till the file download is completed before exiting
        Thread.sleep(2000);
    }

    /**
     * Search for file in self storage
     */
    @Test
    void testFileSearch() {
        FileHandler fHandler = new FileHandler(clientCacheDir, clientLocalDir, cacheSize, clientPort);
        List<String> matchingFiles = fHandler.searchForFile("s");
        matchingFiles.forEach(System.out::println);
    }

    /**
     * Create required space in the cache storage
     */
    @Test
    void testCacheSpace() {
        FileHandler fHandler = new FileHandler(clientCacheDir, clientLocalDir, cacheSize, clientPort);
        fHandler.makeCacheSpace(3000000);
    }

}
