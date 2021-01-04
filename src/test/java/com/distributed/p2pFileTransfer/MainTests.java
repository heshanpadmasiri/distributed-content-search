package com.distributed.p2pFileTransfer;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.List;


class MainTests {
    /**
     * Download file from a node
     *
     * @throws UnknownHostException
     */
    @Test
    void testFileDownload() throws UnknownHostException {
        FileHandler fHandler = new FileHandler();
        InetAddress add = InetAddress.getByName("127.0.0.1");
        Node myNode = new Node(add, 8080);
        fHandler.downloadFile(myNode, "sites.csv", Paths.get(""));
    }

    /**
     * Search for file in self storage
     */
    @Test
    void testFileSearch() {
        FileHandler fHandler = new FileHandler();
        List<String> matchingFiles = fHandler.searchForFile("s");
        matchingFiles.forEach(System.out::println);
    }

    @Test
    void testCacheSpace() {
        FileHandler fileHandler = new FileHandler();
        fileHandler.makeCacheSpace(3000000);
    }

}
