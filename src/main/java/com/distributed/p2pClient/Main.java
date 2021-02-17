package com.distributed.p2pClient;

import com.distributed.p2pFileTransfer.FileDownloadResult;
import com.distributed.p2pFileTransfer.FileNotFoundException;
import com.distributed.p2pFileTransfer.FreeNetFileTransferService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Future;


public class Main {

    public static void main(String[] args){
        System.out.println(" _       __     __                             __           ______               _   __     __ \n" +
                "| |     / /__  / /________  ____ ___  ___     / /_____     / ____/_______  ___  / | / /__  / /_\n" +
                "| | /| / / _ \\/ / ___/ __ \\/ __ `__ \\/ _ \\   / __/ __ \\   / /_  / ___/ _ \\/ _ \\/  |/ / _ \\/ __/\n" +
                "| |/ |/ /  __/ / /__/ /_/ / / / / / /  __/  / /_/ /_/ /  / __/ / /  /  __/  __/ /|  /  __/ /_  \n" +
                "|__/|__/\\___/_/\\___/\\____/_/ /_/ /_/\\___/   \\__/\\____/  /_/   /_/   \\___/\\___/_/ |_/\\___/\\__/  \n" +
                "                                                                                               ");

        String configPath = args[0];
        Properties config  = new Properties();
        File file = new File(configPath);
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));

            String st;
            String[] KeyVal;

            System.out.println("+++++++++++++++++++++++++++++ Configuration +++++++++++++++++++++++++++++");
            while ((st = br.readLine()) != null){
                KeyVal = st.split("=");
                config.setProperty(KeyVal[0],KeyVal[1]);
                System.out.println(KeyVal[0] + ":" + KeyVal[1]);
            }
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println();
            FreeNetFileTransferService client = FreeNetFileTransferService.getInstance(config);
            String helpString = String.join(System.lineSeparator()
                    ,""
                    , "search   <filename> : Search the matching files in the Grid,"
                    , "download <filename> : Download the file from the Grid"
                    , "exit                : Remove the current node from the Grid and exit"
            );
            Scanner scanner = new Scanner(System.in);
            while (true){
                System.out.println();
                System.out.print(">> Enter command: ");
                String query = scanner.nextLine();
                String[] arguments = query.split(" ",2);
                String command = arguments[0];
                String fileName = "";
                if (arguments.length == 2){
                    fileName = arguments[1];
                }
                switch (command){
                    case "help":
                        System.out.println(helpString);
                        break;
                    case "exit":
                        System.out.println(">> Exiting....");
                        System.exit(0);
                    case "search":
                        Future<List<String>> queryResultFuture = client.searchForFile(fileName);
                        List<String> queryResult = queryResultFuture.get();
                        if (queryResult.size() != 0) {
                            for(String name : queryResult) {
                                System.out.println("----> " + name);
                            }
                        }
                        else {
                            System.out.println(">> No matching files found");
                        }
                        break;
                    case "download":
                        Future<FileDownloadResult> downloadResponseFuture = client.downloadFile(fileName);
                        if (downloadResponseFuture != null){
                            FileDownloadResult downloadResult = downloadResponseFuture.get();
                            String body = downloadResult.getBody();
                            switch (downloadResult.getState()){
                                case 0:
                                    System.out.printf(">> Success: %s%n",body);
                                    break;
                                case 1:
                                case 2:
                                    System.out.printf(">> Error: %s%n",body);
                                    break;
                                default:
                                    System.out.println(">> Issue with File Download");
                                    break;
                            }
                        }
                        else{
                            System.out.println(">> Couldn't find an exact match for the filename");
                        }
                        break;
                    default:
                        System.out.println("Illegal command");
                        System.out.println(helpString);
                        break;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}