package com.distributed.p2pFileTransfer;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;


public class FileHandler{
    private String cacheDir;
    private String localDir;
    private long cacheSize;
    private final Storage fileStorage;
    private final ExecutorService executorService;
    private final Logger logger;

    public FileHandler(String cacheDir, String localDir, long cacheSize,String port) {
        this.cacheDir = cacheDir;
        this.localDir = localDir;
        this.cacheSize = cacheSize;
        logger = Logger.getLogger(this.getClass().getName());
        this.fileStorage = new Storage(cacheDir, localDir, cacheSize, this.getClass().getName());
        executorService = Executors.newCachedThreadPool();
        runServer(port);

    }

    /**
     * Start the File Server
     */
    public static void runServer(String port){
        SpringApplication server = new SpringApplication(com.distributed.p2pFileTransfer.Main.class);
        server.setDefaultProperties(Collections.singletonMap("server.port", String.valueOf(port)));
        server.run();
        System.out.println("File server listening to incoming connections... on port " + port);
    }


    /**
     * Concrete implementation of file download
     *
     * @param source      node from which to download the file
     * @param fileName    name of the file
     * @param destination download destination
     * @return FileDownload result which encapsulate errors if they occur during download
     */
    private Future<FileDownloadResult> downloadFile(Node source, String fileName, String destination) {

        FileDownloadCallable task = new FileDownloadCallable(source, fileName, destination, this.fileStorage, this.getClass().getName());
        Future<FileDownloadResult> result = this.executorService.submit(task);
        this.logger.log(Level.INFO, String.format("Attempting to download %s to %s ",fileName,destination));
        if (result.isDone()) {
            try {
                FileDownloadResult response = result.get();
                switch (response.getState()){
                    case 0:
                        this.logger.log(Level.INFO, String.format("%s downloadFile completed to %s ",fileName,destination));
                        break;
                    case 1:
                    case 2:
                        this.logger.log(Level.INFO, String.format("File Download failed: %s", response.getBody()));
                        break;
                    default:
                        this.logger.log(Level.INFO, "Issue with File Download");
                        break;
                }
                this.logger.log(Level.INFO, String.valueOf(response.getBody()));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Download a file to the local directory. Used to download a file directly
     *
     * @param source node from which to download the file
     * @param filename name of the file
     * @return FileDownload result which encapsulate errors if they occur during download
     */
    protected Future<FileDownloadResult> downloadFileToLocal(Node source, String filename){
        return downloadFile(source, filename, this.localDir);
    }

    /**
     * Download a file to the cache directory. Used to cache a file
     *
     * @param source node from which to download the file
     * @param filename name of the file
     * @return FileDownload result which encapsulate errors if they occur during download
     */
    protected Future<FileDownloadResult> downloadFileToCache(Node source, String filename){
        return downloadFile(source, filename, this.cacheDir);
    }

    /**
     * Make enough space in cache for the new file
     *
     * @param fileSize Size of the new file
     */
    protected void makeCacheSpace(long fileSize) {
        fileStorage.makeCacheSpace(fileSize);
    }

    /**
     * Used to search for files in local storage and cache
     *
     * @param query search query
     * @return list of file names matching the query
     */
    protected List<String> searchForFile(String query) {
        return fileStorage.searchForFile(query);
    }

}


