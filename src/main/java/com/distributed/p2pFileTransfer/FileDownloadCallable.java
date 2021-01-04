package com.distributed.p2pFileTransfer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class FileDownloadCallable implements Callable<FileDownloadResult> {
    private Node source;
    private String fileName;

    FileDownloadCallable(Node source, String filename) {
        this.source = source;
        this.fileName = filename;
    }

    /**
     * Create a callable for making a GET request to download the file
     *
     * @return FileDownloadResult Download status
     * @throws IOException
     */
    @Override
    public FileDownloadResult call() throws IOException {
        FileDownloadResult result;

        URL url = new URL("http://" + source.getIpAddress().getHostAddress() + ":" + source.getPort() + "/file/" + fileName);

        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


        String fileHash = httpURLConnection.getHeaderField("Hash");
        int fileSize = httpURLConnection.getContentLength();
        if (fileHash != null) {
            result = downloadFile(httpURLConnection, fileHash, fileSize);
        } else {
            result = new FileDownloadResult("Hash not avaialable", 1);
        }
        return result;
    }

    /**
     * Download the file and verify the hash
     *
     * @param httpURLConnection
     * @param fileHash          Hash of the file sent from the server
     * @param fileSize          Size of the file
     * @return FileDownloadResult Download status
     * @throws IOException
     */
    public FileDownloadResult downloadFile(HttpURLConnection httpURLConnection, String fileHash, int fileSize) throws IOException {
        InputStream inputStream = httpURLConnection.getInputStream();
        // Byte reader
        // TODO: Get download directory from config
        String saveFilePath = "/home/kalana/distributed/content/local_storage/" + fileName;

        FileOutputStream outputStream = new FileOutputStream(saveFilePath);
        int bytesRead = -1;
        byte[] buffer = new byte[fileSize];
        if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            //TODO: Fix this to work wth empty files
            //int bytestRead = inputStream.read(buffer);
            if (inputStream.available() != 0) {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                System.out.println("Downloaded " + saveFilePath);
                String hexHash = Storage.getFileHash(saveFilePath);
                if (hexHash.equals(fileHash)) {
                    System.out.println("File hashes match");
                    return new FileDownloadResult("success", 0);
                } else {
                    System.out.println("File hashes do not match");
                    return new FileDownloadResult("Hash do not match", 1);
                }
            } else {
                return new FileDownloadResult("Input stream empty", 0);
            }


        } else {
            return new FileDownloadResult(httpURLConnection.getResponseMessage(), 1);
        }
    }

}
