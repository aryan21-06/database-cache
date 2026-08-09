package com.dbcache;
import java.sql.SQLException;

import com.dbcache.cache.*;
import com.dbcache.database.*;
import com.dbcache.benchmark.*;
import com.dbcache.config.*;
import com.dbcache.ui.*;
import com.dbcache.model.*;
import com.dbcache.handler.*;

import javax.swing.*;

public class Main {
public static void main(String[] args) {
    // 1. Load config
    Config.load();
    
    // 2. Create single DatabaseManager instance
    try{
        DatabaseManager dbManager = new DatabaseManager(
            Config.getDbUrl(),
            Config.getDbUsername(),
            Config.getDbPassword()
        );
    }catch(SQLException se){
        System.out.println("Database Error" + se.getMessage());
        
    // 3. Create cache and request handler (pass dbManager)
    CacheConfig cacheConfig = new CacheConfig(
        Config.getCacheCapacity(),
        Config.getCacheTtlSeconds(),
        EvictionPolicy.LRU
    );
    RequestHandler requestHandler = new RequestHandler(dbManager, cacheConfig);
    
    // 4. Create benchmark runner (pass both)
    BenchmarkRunner benchmarkRunner = new BenchmarkRunner(requestHandler, dbManager);
    
    // 5. Launch UI (pass handlers)
    SwingUtilities.invokeLater(() -> {
        MainFrame frame = new MainFrame(requestHandler, benchmarkRunner);
        frame.setVisible(true);
    });
}
}
