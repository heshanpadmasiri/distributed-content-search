package com.distributed.p2psearch;

import org.apache.tomcat.util.buf.HexUtils;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URLConnection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.util.FileCopyUtils;

@RestController
public class EndPointController {

    @RequestMapping("/rest")
    public String helloFile(@RequestParam(value ="filename") String filename){
        return filename;
    }
    @RequestMapping("/hello")
    public String helloWorld(){
        return "hello!";
    }
    @RequestMapping("/file/{name:.+}")
    public void getFile(HttpServletRequest req, HttpServletResponse res, @PathVariable("name") String name) throws IOException {
        System.out.println("Filename: " + name);
        String filepath = "/home/kalana/sites";
        File file = new File(filepath);
        MessageDigest md = null;
        if (file.exists()) {
            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
            if (mimeType == null) {
                //unknown mimetype so set the mimetype to application/octet-stream
                mimeType = "application/octet-stream";
            }
            res.setContentType(mimeType);
            res.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));
            res.setContentLength((int) file.length());

            InputStream input = new BufferedInputStream(new FileInputStream(file));
            try{
                md = MessageDigest.getInstance("md5");
                md.update(Files.readAllBytes(Paths.get(filepath)));
                byte[] hash = md.digest();
                String hexHash = HexUtils.toHexString(hash);
                res.setHeader("Hash",hexHash);
            }
            catch (NoSuchAlgorithmException e){
                e.printStackTrace();
            }
            FileCopyUtils.copy(input, res.getOutputStream());

        }
    }
}
