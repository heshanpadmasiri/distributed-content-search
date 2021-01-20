package com.distributed.p2pFileTransfer;

import org.springframework.boot.SpringApplication;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;


public class FileHandler {
    //private AbstractFileTransferService fileTransferService;
    private String cacheDir;
    private String localDir;
    private long cacheSize;
    private final Storage fileStorage;
    private final ExecutorService executorService;
    private final Logger logger;

    public FileHandler(String cacheDir, String localDir, long cacheSize) {
        this.cacheDir = cacheDir;
        this.localDir = localDir;
        this.cacheSize = cacheSize;
        logger = Logger.getLogger(this.getClass().getName());
        this.fileStorage = new Storage(cacheDir, localDir, cacheSize, this.getClass().getName());
        executorService = Executors.newFixedThreadPool(1);
        runServer();

    }

    /**
     * Start the File Server
     */
    public static void runServer(){
        SpringApplication.run(com.distributed.p2pFileTransfer.Main.class);
        System.out.println("File server listening to incoming connections...");
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
        if (result.isDone()) {
            try {
                System.out.println(result.get());
                this.logger.log(Level.INFO, String.valueOf(result.get()));
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
