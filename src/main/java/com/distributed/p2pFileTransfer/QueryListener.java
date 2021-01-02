package com.distributed.p2pFileTransfer;

class QueryListener implements Runnable {
    private final AbstractFileTransferService fileTransferService;

    public QueryListener(AbstractFileTransferService fileTransferService) {
        this.fileTransferService = fileTransferService;
    }

    @Override
    public void run() {

    }
}
