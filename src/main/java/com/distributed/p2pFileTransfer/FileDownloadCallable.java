package com.distributed.p2pFileTransfer;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class FileDownloadCallable implements Callable<FileDownloadResult> {
    private Node source;
    private String fileName;

    FileDownloadCallable(Node source, String filename){
        this.source = source;
        this.fileName = filename;
    }

    @Override
    public FileDownloadResult call() throws Exception {
        // it may make sense to directly throw the exception here rather than at the file transfer service
        // Create a neat value object to hold the URL
        StringBuilder urlString = new StringBuilder("http://" + source.getIpAddress().getHostAddress() + ":" + source.getPort() + "/" + fileName);
        URL url = new URL(urlString.toString());

        // Open a connection(?) on the URL(??) and cast the response(???)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Now it's "open", we can set the request method, headers etc.
        //connection.setRequestProperty("accept", "application/json");

        // This line makes the request
        InputStream responseStream = connection.getInputStream();

        System.out.println(responseStream.read());
        responseStream.close();
        return new FileDownloadResult("hello", 0);
    }
}
