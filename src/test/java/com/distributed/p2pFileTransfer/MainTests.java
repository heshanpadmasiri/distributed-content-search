package com.distributed.p2pFileTransfer;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;


class MainTests {
    /**
     * Download file from a node
     *
     * @throws UnknownHostException
     */
    @Test
    void testFileDownload() throws UnknownHostException {
        FileHandler fHandler = new FileHandler("/home/kalana/distributed/content/cache_storage", "/home/kalana/distributed/content/local_storage", 10000000);
        InetAddress add = InetAddress.getByName("127.0.0.1");
        Node myNode = new Node(add, 8080);
        fHandler.downloadFile(myNode, "sites.csv", "/home/kalana/distributed/content/cache_storage");
    }

    /**
     * Search for file in self storage
     */
    @Test
    void testFileSearch() {
        FileHandler fHandler = new FileHandler("/home/kalana/distributed/content/cache_storage", "/home/kalana/distributed/content/local_storage", 10000000);
        List<String> matchingFiles = fHandler.searchForFile("s");
        matchingFiles.forEach(System.out::println);
    }

    /**
     * Create required space in the cache storage
     */
    @Test
    void testCacheSpace() {
        FileHandler fileHandler = new FileHandler("/home/kalana/distributed/content/cache_storage", "/home/kalana/distributed/content/local_storage", 10000000);
        fileHandler.makeCacheSpace(3000000);
    }

}
