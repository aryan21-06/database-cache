package com.dbcache.benchmark;

import java.sql.SQLException;
import java.util.List;

import com.dbcache.cache.CacheConfig;
import com.dbcache.cache.EvictionPolicy;
import com.dbcache.database.DatabaseManager;
import com.dbcache.handler.RequestHandler;
import com.dbcache.model.QueryResponse;

public class BenchmarkRunner {
    
    private final RequestHandler requestHandler;
    private final DatabaseManager dbManager;
    
    public BenchmarkRunner(RequestHandler requestHandler, DatabaseManager dbManager) {
        // TODO: Initialize benchmark runner
        this.requestHandler = requestHandler;
        this.dbManager = dbManager;
    }
    
    public BenchmarkResults runBenchmark(BenchmarkConfig config) {
        WorkloadGenerator workloadGenerator = new WorkloadGenerator();
        List<String> workload = workloadGenerator
        						.generateWorkload(
        								config.getTotalRequests(),
        								config.getUniqueQueries()
        								);
        
        BenchmarkMetrics noCache = runWithoutCache(workload);
        requestHandler.clearCache();
        requestHandler.configureCache(new CacheConfig(
        											config.getCacheCapacity(),
        											config.getTtlSeconds(), 
        											EvictionPolicy.LRU
        											)
        								);
        
        BenchmarkMetrics withCache = runWithCache(workload);
        
        return new BenchmarkResults(noCache,withCache);
    }

    private BenchmarkMetrics runWithoutCache(List <String> workload){
    	long startTime = System.currentTimeMillis();
    	int dbQueries=0;
    	for(String sql:workload) {
	    	try {
	    		dbManager.executeQuery(sql);
	    		dbQueries++;
	    		}
	    	 catch(SQLException e) {
	    		System.out.println(e.getMessage());
	    	}
    	}
    	long finalTime = System.currentTimeMillis() - startTime;
    	double avgLatency = (double) finalTime / workload.size();
    	return new BenchmarkMetrics(workload.size(),dbQueries,0,workload.size(),0.0,finalTime,avgLatency);
        
    }
    
    private BenchmarkMetrics runWithCache(List <String> workload){
    	long startTime = System.currentTimeMillis();
    	int dbQueries=0;
    	
        long cacheHits = 0;
        long cacheMisses = 0;
      
        for (String sql : workload) {
            QueryResponse response = requestHandler.executeQuery(sql);
        
            if (response.isCacheHit()) {
                cacheHits++;
            } else {
                cacheMisses++;
                if (response.isDatabaseAccessed()) {
                    dbQueries++;
                }
            }
        }
      
        long totalTime = System.currentTimeMillis() - startTime;
        double avgLatency = (double) totalTime / workload.size();
        double hitRate = (double) cacheHits / workload.size();
      
        return new BenchmarkMetrics(
            workload.size(),
            dbQueries,
            cacheHits,
            cacheMisses,
            hitRate,
            totalTime,
            avgLatency
        );
    }
        
    }
