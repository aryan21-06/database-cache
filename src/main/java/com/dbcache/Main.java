package com.dbcache;
import java.sql.SQLException;
import java.io.IOException;

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
    try {
        Config.load();
    } catch (IOException e) {
        System.out.println("Config Error: " + e.getMessage());
        return;
    }

    DatabaseManager dbManager;
    try{
        dbManager = new DatabaseManager(
            Config.getDbUrl(),
            Config.getDbUsername(),
            Config.getDbPassword()
        );
    }catch(SQLException se){
        System.out.println("Database Error" + se.getMessage());
        return;
    }

    CacheConfig cacheConfig = new CacheConfig(
        Config.getCacheCapacity(),
        Config.getCacheTtlSeconds(),
        EvictionPolicy.LRU
    );
    RequestHandler requestHandler = new RequestHandler(dbManager, cacheConfig);

    BenchmarkRunner benchmarkRunner = new BenchmarkRunner(requestHandler, dbManager);

    SwingUtilities.invokeLater(() -> {
        MainFrame frame = new MainFrame(requestHandler, benchmarkRunner);
        frame.setVisible(true);
    });
}
}
