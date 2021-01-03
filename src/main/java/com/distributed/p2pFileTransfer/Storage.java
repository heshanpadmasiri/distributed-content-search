package com.distributed.p2pFileTransfer;

import org.apache.tomcat.util.buf.HexUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Storage {
    // TODO: Get file directory from config

    /**
     * Used to search for file matching a given file Name in the storage
     *
     * @param fileName search query
     * @return List of file names matching the query
     */
    List<String> searchForFile(String fileName) {
        return null;
    }

    /**
     * Used to get a file from storage
     * @param filepath name of the file
     * @return file
     * @throws FileNotFoundException if no file matches the file name exactly
     */
    public static File getFile(String filepath) throws FileNotFoundException{
        File file = new File(filepath);
        if (file.exists()){
            return file;
        }
        else{
            throw new FileNotFoundException();
        }
    };
    /**
     * Used to get the hash of file from storage
     * @param filepath name of the file
     * @return SHA-1 hash of the file
     * @throws FileNotFoundException if no file matches the file name exactly
     */
    public static String getFileHash(String filepath) throws FileNotFoundException{
        File file = new File(filepath);
        if (file.exists()){
            try{
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                md.update(Files.readAllBytes(Paths.get(filepath)));
                byte[] hash = md.digest();
                return HexUtils.toHexString(hash);
            }
            catch (NoSuchAlgorithmException | IOException e){
                e.printStackTrace();
                return null;
            }
        }
        else {
            throw new FileNotFoundException();
        }

    };
}
