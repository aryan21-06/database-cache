package com.dbcache.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    
    private static final Properties props = new Properties();
    private static final String CONFIG_FILE = "config.properties";
    
    public static void load() throws IOException {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            props.load(input);
        }
    }
    
    public static String getDbUrl() {
        return props.getProperty("db.url");
    }
    
    public static String getDbUsername() {
        return props.getProperty("db.username");
    }
    
    public static String getDbPassword() {
        return props.getProperty("db.password");
    }
    
    public static int getCacheCapacity() {
        return Integer.parseInt(props.getProperty("cache.capacity", "100"));
    }
    
    public static long getCacheTtlSeconds() {
        return Long.parseLong(props.getProperty("cache.ttl.seconds", "60"));
    }
}
