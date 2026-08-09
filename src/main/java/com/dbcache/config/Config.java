package com.dbcache.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    
    private static Properties props = new Properties();
    
    public static void load() throws IOException {
        // TODO: Load config.properties
    }
    
    public static String getDbUrl() {
        // TODO: Return database URL
        return null;
    }
    
    public static String getDbUsername() {
        // TODO: Return database username
        return null;
    }
    
    public static String getDbPassword() {
        // TODO: Return database password
        return null;
    }
    
    public static int getCacheCapacity() {
        // TODO: Return cache capacity
        return 100;
    }
    
    public static long getCacheTtlSeconds() {
        // TODO: Return cache TTL in seconds
        return 60;
    }
}
