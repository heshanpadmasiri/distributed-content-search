package com.distributed.p2pClient;

import com.distributed.p2pFileTransfer.FreeNetFileTransferService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Future;


public class Main {

    public static void main(String[] args) throws Exception{
        String configPath = args[0];
        Properties config  = new Properties();
        File file = new File(configPath);

        BufferedReader br = new BufferedReader(new FileReader(file));

        String st;
        String[] KeyVal;
        while ((st = br.readLine()) != null){
            KeyVal = st.split("=");
            config.setProperty(KeyVal[0],KeyVal[1]);
            System.out.println(KeyVal[0] + ":" + KeyVal[1]);
        }
        FreeNetFileTransferService client = FreeNetFileTransferService.getInstance(config);


        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.println("Enter command: ");
            String command = scanner.nextLine();
            switch (command){
                case "exit":
                    System.out.println("Exiting....");
                    System.exit(0);
                case "search":
                    System.out.println("Enter file name: ");
                    String fileName = scanner.nextLine();
                    Future<List<String>> queryResultFuture = client.searchForFile(fileName);
                    List<String> queryResult = queryResultFuture.get();
                    for(String name : queryResult) {
                        System.out.println("-- " + file);
                    }
                    break;


            }
        }

    }
}
