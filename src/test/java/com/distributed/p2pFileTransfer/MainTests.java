package com.distributed.p2pFileTransfer;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;


class MainTests {

    String cacheDir = "/home/kalana/distributed/content/cache_storage";
    String localDir = "/home/kalana/distributed/content/local_storage";
    long cacheSize = 10000000;


    /**
     *  Start Web Server
     */
    @Test
    void startWebServer(){
        FileHandler fHandler = new FileHandler(cacheDir, localDir , cacheSize);
        while (true){
            int a = 1;
        }
    }
    /**
     * Download file from a node
     *
     * @throws UnknownHostException
     */
    @Test
    void testFileDownload() throws UnknownHostException {
        FileHandler fHandler = new FileHandler(cacheDir, localDir , cacheSize);
        InetAddress add = InetAddress.getByName("127.0.0.1");
        Node myNode = new Node(add, 8080);
        fHandler.downloadFileToLocal(myNode, "sites.csv");
        //fHandler.downloadFileToCache(myNode, "sites.csv");
    }

    /**
     * Search for file in self storage
     */
    @Test
    void testFileSearch() {
        FileHandler fHandler = new FileHandler(cacheDir, localDir , cacheSize);
        List<String> matchingFiles = fHandler.searchForFile("s");
        matchingFiles.forEach(System.out::println);
    }

    /**
     * Create required space in the cache storage
     */
    @Test
    void testCacheSpace() {
        FileHandler fHandler = new FileHandler(cacheDir, localDir , cacheSize);
        fHandler.makeCacheSpace(3000000);
    }

}
