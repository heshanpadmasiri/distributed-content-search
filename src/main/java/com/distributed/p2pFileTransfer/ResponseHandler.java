package com.distributed.p2pFileTransfer;

import java.util.HashMap;

public class ResponseHandler {

    public HashMap<String,String> handleRegisterResponse(String body) {

        String [] params = body.split(" ");
        String no_nodes = params[2];

        HashMap<String,String> response = new HashMap<>();
        response.put("no_nodes",no_nodes);
        if(no_nodes.equals("0")) {
            System.out.println("request is successful, no nodes in the system");
        }
        else if(no_nodes.equals("1")) {
            //System.out.println("request is successful,1 node in the system");
            response.put("IP_1",params[3]);
            response.put("port_1",params[4]);
        }
        else if(no_nodes.equals("2")) {
            response.put("IP_1",params[3]);
            response.put("port_1",params[4]);
            response.put("IP_2",params[5]);
            response.put("port_3",params[6]);
        }
        return response;
    }

//    private void formatResponse() {
//
//    }
}
