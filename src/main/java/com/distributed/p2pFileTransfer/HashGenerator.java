package com.distributed.p2pFileTransfer;

import org.apache.tomcat.util.buf.HexUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashGenerator {
    public static String generateHash(String filepath){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(Files.readAllBytes(Paths.get(filepath)));
            byte[] hash = md.digest();
            return HexUtils.toHexString(hash);
        }
        catch (NoSuchAlgorithmException | IOException e){
            e.printStackTrace();
            return "";
        }
    }
}
