package com.distributed.p2pFileTransfer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class QueryListener implements Runnable {
    private final AbstractFileTransferService fileTransferService;
    private ExecutorService executorService;

    public QueryListener(AbstractFileTransferService fileTransferService) {
        this.fileTransferService = fileTransferService;
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {

    }
}
