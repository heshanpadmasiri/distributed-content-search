package com.distributed.p2pFileTransfer;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FileHandler {
    //private AbstractFileTransferService fileTransferService;
    private final String cacheDir;
    private final String localDir;
    private final long cacheSize;
    private final Storage fileStorage;

    FileHandler(String cacheDir, String localDir, long cacheSize) {
        this.cacheDir = cacheDir;
        this.localDir = localDir;
        this.cacheSize = cacheSize;
        this.fileStorage = new Storage(cacheDir, localDir, cacheSize);
    }

    /**
     * Concrete implementation of file download
     *
     * @param source      node from which to download the file
     * @param fileName    name of the file
     * @param destination download destination
     * @return FileDownload result which encapsulate errors if they occur during download
     */
    protected Future<FileDownloadResult> downloadFile(Node source, String fileName, String destination) {

        FileDownloadCallable task = new FileDownloadCallable(source, fileName, destination, this.fileStorage);

        ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        Future<FileDownloadResult> result = executorService.submit(task);
        if (result.isDone()) {
            try {
                System.out.println(result.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return result;
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
