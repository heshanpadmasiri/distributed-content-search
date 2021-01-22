package com.distributed.p2pFileTransfer;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Future;

public class FreeNetFileTransferService extends AbstractFileTransferService{
    @Override
    public Future<List<String>> searchForFile(String query) {
        return null;
    }

    @Override
    public void downloadFile(String fileName, Path destination) throws FileNotFoundException, DestinationAlreadyExistsException {

    }

    @Override
    public void downloadFileFrom(String fileName, Path destination, Node source) throws FileNotFoundException, DestinationAlreadyExistsException, NodeNotFoundException {

    }
}
