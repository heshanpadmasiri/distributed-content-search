package com.distributed.p2pFileTransfer;

import org.apache.tomcat.util.buf.HexUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class Storage {
    // TODO: Get file directory from config
//    private static final String dir = "/home/kalana/distributed/content/";
//    private static final String cacheDir = dir + "cache_storage/";
//    private static final String localDir = dir + "local_storage/";
//    private static final long fullCacheSize = 10000000; // 10MB
    private String cacheDir;
    private String localDir;
    private long fullCacheSize;

    Storage(String cacheDir,String localDir, long fullCacheSize){
        this.cacheDir = cacheDir;
        this.localDir = localDir;
        this.fullCacheSize = fullCacheSize;
    }

    /**
     * Make enough space in the cache directory to download the new file
     *
     * @param reqSpace Size of the new file in bytes
     */
    public void makeCacheSpace(long reqSpace) {
        File cacheDirFile = new File(this.cacheDir);
        long cacheSize = FileUtils.sizeOfDirectory(cacheDirFile);
        while ((this.fullCacheSize - cacheSize) < reqSpace) {
            deleteOldestFile(cacheDirFile);
            cacheSize = FileUtils.sizeOfDirectory(cacheDirFile);
        }
    }

    /**
     * Delete oldest file in the directory
     *
     * @param directory Directory
     */
    public void deleteOldestFile(File directory) {
        File[] dirFiles = directory.listFiles();
        long oldestDate = Long.MAX_VALUE;
        File oldestFile = null;
        if (dirFiles != null) {
            for (File f : dirFiles) {
                if (f.lastModified() < oldestDate) {
                    oldestDate = f.lastModified();
                    oldestFile = f;
                }
            }
            if (oldestFile != null) {
                System.out.println("Deleted file " + oldestFile.getName());
                oldestFile.delete();
            }
        }
    }

    /**
     * Search the directory for the file existence
     *
     * @param searchDir Directory to search
     * @param fileName  File to search
     * @return String file path if exists else null
     */
    public String searchDirectory(String searchDir, String fileName) {
        String[] searchDirListing = new File(searchDir).list();
        if (searchDirListing != null) {
            for (String filename : searchDirListing) {
                if (filename.matches(fileName)) {
                    return searchDir + "/" + fileName;
                }
            }
        }
        return null;
    }

    /**
     * Get path of the file
     *
     * @param fileName Name of the file
     * @return String File Path
     */
    public String getFilePath(String fileName) {
        String filepath = searchDirectory(localDir, fileName);
        if (filepath == null) {
            filepath = searchDirectory(cacheDir, fileName);
        }
        return filepath;

    }

    /**
     * Used to search for file matching a given file Name in the storage
     *
     * @param query search query
     * @return List of file names matching the query
     */
    public List<String> searchForFile(String query) {

        String[] cacheFileDir = new File(cacheDir).list();
        String[] localFileDir = new File(localDir).list();
        List<String> matches = new ArrayList<>();

        String regex = "(.*)" + query + "(.*)";
        if (cacheFileDir != null) {
            for (String filename : cacheFileDir) {
                if (filename.matches(regex)) {
                    matches.add(filename);
                }
            }
        }
        if (localFileDir != null) {
            for (String filename : localFileDir) {
                if (filename.matches(regex)) {
                    matches.add(filename);
                }
            }
        }
        return matches;
    }

    /**
     * Used to get a file from storage
     *
     * @param fileName name of the file
     * @return file
     * @throws FileNotFoundException if no file matches the file name exactly
     */
    public File getFile(String fileName) throws FileNotFoundException {
        String filePath = this.getFilePath(fileName);
        File file = new File(filePath);
        if (file.exists()) {
            return file;
        } else {
            throw new FileNotFoundException();
        }
    }

    /**
     * Used to get the hash of file from storage
     *
     * @param fileName name of the file
     * @return SHA-1 hash of the file
     * @throws FileNotFoundException if no file matches the file name exactly
     */
    public String getFileHash(String fileName) throws FileNotFoundException {
        String filePath = this.getFilePath(fileName);
        File file = new File(filePath);
        if (file.exists()) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                md.update(Files.readAllBytes(Paths.get(filePath)));
                byte[] hash = md.digest();
                return HexUtils.toHexString(hash);
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            throw new FileNotFoundException();
        }

    }

    ;
}
