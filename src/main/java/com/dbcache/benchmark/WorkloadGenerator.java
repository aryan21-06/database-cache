package com.dbcache.benchmark;

import java.util.List;

public class WorkloadGenerator {
    
    private final QueryBuilder queryBuilder;
    private final DistributionEngine distributionEngine;
    private final ZipfianDistribution zipfianDistribution;
    
    public WorkloadGenerator() {
        this.queryBuilder = new QueryBuilder();
        this.distributionEngine = new DistributionEngine();
        this.zipfianDistribution = new ZipfianDistribution();
    }
    
    public List<String> generateWorkload(int totalRequests, int uniqueQueries) {
        return generateWorkload(totalRequests, uniqueQueries, DistributionType.ZIPFIAN);
    }
    
    public List<String> generateWorkload(int totalRequests, int uniqueQueries, DistributionType type) {
        List<String> queries = queryBuilder.generateUniqueQueries(uniqueQueries);
        if (type == DistributionType.ZIPFIAN) {
            return zipfianDistribution.applyZipfianDistribution(queries, totalRequests);
        }
        return distributionEngine.applyModerateDistribution(queries, totalRequests);
    }
}
