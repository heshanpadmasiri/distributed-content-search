package com.distributed.p2pFileTransfer;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Main {
    public static void main(String[] args)  {
        SpringApplication.run(com.distributed.p2pFileTransfer.Main.class, args);
        System.out.println("Web server listening to incoming connections...");
    }
}
