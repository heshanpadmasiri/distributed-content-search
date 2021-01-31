package com.distributed.p2pFileTransfer;

import java.util.Properties;

public class Configuration {
    private static String port;
    private static String cacheDir;
    private static String localDir;
    private static long cacheSize;
    private static String bootstrapServerIp;
    private static String bootstrapServerport;

    public static void setConfiguration(Properties configuration){
        cacheDir = configuration.getProperty("cache_dir");
        localDir =  configuration.getProperty("local_dir");
        cacheSize = Integer.parseInt(configuration.getProperty("cache_size"));
        port = configuration.getProperty("port");
        bootstrapServerIp = configuration.getProperty("boostrap_server_ip");
        bootstrapServerport = configuration.getProperty("boostrap_server_port");
    }

    public static String getPort() {
        return port;
    }

    public static String getCacheDir() {
        return cacheDir;
    }

    public static String getLocalDir() {
        return localDir;
    }

    public static long getCacheSize() {
        return cacheSize;
    }

    public static String getBootstrapServerIp() {
        return bootstrapServerIp;
    }

    public static String getBootstrapServerport() {
        return bootstrapServerport;
    }
}
