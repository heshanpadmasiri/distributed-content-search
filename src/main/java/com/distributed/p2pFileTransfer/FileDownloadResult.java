package com.distributed.p2pFileTransfer;

public class FileDownloadResult extends Result {
    /**
     * Used to represent a file download result within the system. Never use directly but
     * extend to represent specific result types
     *
     * @param body  representing the result
     * @param state representing the state of result.
     *              0 : success
     *              1 : download failure due to network
     *              2 : download failure due to storage issues,
     */
    public FileDownloadResult(String body, int state) {
        super(body, state);
    }
}
