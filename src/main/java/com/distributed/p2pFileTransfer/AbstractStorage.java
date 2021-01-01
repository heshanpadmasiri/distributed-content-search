package com.distributed.p2pFileTransfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

abstract class AbstractStorage {
    /**
     * Used to search for file matching a given file Name in the storage
     * @param fileName search query
     * @return List of file names matching the query
     */
    abstract List<String> searchForFile(String fileName);

    /**
     * Used to get a file from storage
     * @param fileName name of the file
     * @return file
     * @throws FileNotFoundException if no file matches the file name exactly
     */
    abstract File getFile(String fileName) throws FileNotFoundException;
    /**
     * Used to get the hash of file from storage
     * @param fileName name of the file
     * @return SHA-256 hash of the file
     * @throws FileNotFoundException if no file matches the file name exactly
     */
    abstract String getFileHash(String fileName) throws FileNotFoundException;
}
