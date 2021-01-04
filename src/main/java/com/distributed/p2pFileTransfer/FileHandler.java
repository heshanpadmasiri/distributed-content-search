package com.distributed.p2pFileTransfer;


import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FileHandler {
    //private AbstractFileTransferService fileTransferService;

    /**
     * Concrete implementation of file download
     * @param source node from which to download the file
     * @param fileName name of the file
     * @param destination download destination
     * @return FileDownload result which encapsulate errors if they occur during download
     */
    protected Future<FileDownloadResult> downloadFile(Node source, String fileName, Path destination) {

        FileDownloadCallable task = new FileDownloadCallable(source,fileName);

        ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        Future<FileDownloadResult> result = executorService.submit(task);
        if (result.isDone()){
            try{
                System.out.println(result.get());
            } catch (InterruptedException | ExecutionException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    //TODO: check cache space before starting automated download

    /**
     * Used to search for files in local storage and cache
     * @param query search query
     * @return list of file names matching the query
     */
    protected List<String> searchForFile(String query){
        return Storage.searchForFile(query);
    }

}
