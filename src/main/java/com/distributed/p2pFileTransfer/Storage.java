package com.distributed.p2pFileTransfer;

import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.buf.HexUtils;

import java.io.FileNotFoundException;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Storage {

    private final Logger logger;
    private final String fileListName = "filelist.txt";
    private String cacheDir;
    private String localDir;
    private long fullCacheSize;

    Storage(String cacheDir, String localDir, long fullCacheSize, String loggerName) {
        this.cacheDir = cacheDir;
        this.localDir = localDir;
        this.fullCacheSize = fullCacheSize;
        this.logger = Logger.getLogger(loggerName);
    }

    /**
     * Convert file content to a byte array
     *
     * @param file File
     * @return byte[]
     */
    private static byte[] readContentIntoByteArray(File file) {
        FileInputStream fileInputStream;
        byte[] bFile = new byte[(int) file.length()];
        try {
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
            for (byte b : bFile) {
                System.out.print((char) b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bFile;
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
                if (!f.getName().equals("filelist.txt")){
                    if (f.lastModified() < oldestDate) {
                        oldestDate = f.lastModified();
                        oldestFile = f;
                    }
                }
            }
            if (oldestFile != null) {
                this.logger.log(Level.INFO, String.format("Deleted file %s", oldestFile.getName()));
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
    public Boolean searchDirectory(String searchDir, String fileName) {
        String thisLine;
        Boolean fileExists = false;
        try {
            BufferedReader br = new BufferedReader(new FileReader(searchDir + File.separator + fileListName));
            while ((thisLine = br.readLine()) != null) {
                if (thisLine.matches(fileName)) {
                    fileExists = true;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileExists;
    }

    /**
     * Check if file exists in the node
     *
     * @param fileName Name of the file
     * @return String File Path
     */
    public Boolean checkFileExists(String fileName) {
        Boolean fileExists = searchDirectory(localDir, fileName);
        if (fileExists == false) {
            fileExists = searchDirectory(cacheDir, fileName);
        }
        return fileExists;
    }

    /**
     * Check if file exists in a directory
     *
     * @param fileName Name of the file
     * @return String File Path
     */
    public Boolean checkFileExists(String fileName, String directory) {
        return searchDirectory(directory, fileName);
    }

    /**
     * Generate a 2-10MB file with random content
     *
     * @param fileName Name of the file to generate
     * @return File Genearated file
     */
    public File generateRandomFile(String fileName) {
        long upperbound = 10000000;
        long lowerBound = 2000000;
        long fileSize = ThreadLocalRandom.current().nextLong(lowerBound, upperbound + 1);
        File file = new File("/tmp/"+fileName);
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.setLength(fileSize);
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Used to search for file matching a given file Name in the storage
     *
     * @param query search query
     * @return List of file names matching the query
     */
    public List<String> searchForFile(String query) {
        Set<String> matches = new HashSet<>();
        String thisLine;
        String regex = "(.*)" + query + "(.*)";
        try {
            BufferedReader cacheFileListingReader = new BufferedReader(new FileReader(cacheDir + File.separator + fileListName));
            BufferedReader localFileListingReader = new BufferedReader(new FileReader(localDir + File.separator + fileListName));
            while ((thisLine = cacheFileListingReader.readLine()) != null) {
                if (thisLine.matches(regex)) {
                    matches.add(thisLine);
                }
            }

            while ((thisLine = localFileListingReader.readLine()) != null) {
                if (thisLine.matches(regex)) {
                    matches.add(thisLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(matches);
    }

    /**
     * Used to get the hash of file from storage
     *
     * @param file
     * @return SHA-1 hash of the file
     * @throws FileNotFoundException if no file matches the file name exactly
     */
    public String getFileHash(File file) throws FileNotFoundException {
        if (file.exists()) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                md.update(readContentIntoByteArray(file));
                byte[] hash = md.digest();
                return HexUtils.toHexString(hash);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            throw new FileNotFoundException();
        }
    }

    /**
     * Update the file listing in the directory
     *
     * @param dir      Directory path
     * @param fileName Filename to update
     * @throws IOException
     */
    public void updateDirectoryListing(String dir, String fileName) throws IOException {
        File file = new File(dir + File.separator + fileListName);
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
        bw.write(fileName);
        bw.newLine();
        bw.flush();
        bw.close();
        logger.log(Level.INFO, String.format("Updated %s directory listing with %s", dir, fileName));
    }

}
