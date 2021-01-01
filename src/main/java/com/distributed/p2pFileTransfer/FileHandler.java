package com.distributed.p2pFileTransfer;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Future;

//todo: implement the end point code here or under this
public class FileHandler {
    private AbstractFileTransferService fileTransferService;
    private AbstractStorage storage;

    /**
     * Concrete implementation of file download
     * @param source node from which to download the file
     * @param fileName name of the file
     * @param destination download destination
     * @return FileDownload result which encapsulate errors if they occur during download
     */
    protected Future<FileDownloadResult> downloadFile(Node source, String fileName, Path destination){
        // it may make sense to directly throw the exception here rather than at the file transfer service
        throw new NotImplementedException();
    }

    /**
     * Used to search for files in local storage
     * @param query search query
     * @return list of file names matching the query
     */
    protected List<String> searchForFile(String query){
        return storage.searchForFile(query);
    }

}
