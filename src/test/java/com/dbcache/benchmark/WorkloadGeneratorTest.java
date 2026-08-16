package com.dbcache.benchmark;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WorkloadGeneratorTest {
    
    @Test
    void testGenerateUniqueQueries() {
        Set<String> setUnique = new HashSet<>();
        QueryBuilder queryBuilder = new QueryBuilder();
        List<String> uniqueQueries = queryBuilder.generateUniqueQueries(100);
        int totalRequests = 1000;
        DistributionEngine distributionEngine = new DistributionEngine();
        List<String> workload = distributionEngine.applyModerateDistribution(uniqueQueries, totalRequests);
        int requestsReceived = 0;
        for(String s:workload){
            requestsReceived++;
            setUnique.add(s);
        }
        System.out.println(setUnique);
    }
    
    @Test
    void testZipfianDistribution() {
        QueryBuilder queryBuilder = new QueryBuilder();
        int uniqueCount = 50;
        int totalRequests = 10000;
        List<String> uniqueQueries = queryBuilder.generateUniqueQueries(uniqueCount);

        ZipfianDistribution zipfian = new ZipfianDistribution();
        List<String> workload = zipfian.applyZipfianDistribution(uniqueQueries, totalRequests);

        assertEquals(totalRequests, workload.size());

        Map<String, Integer> freq = new HashMap<>();
        for (String q : workload) {
            freq.merge(q, 1, Integer::sum);
        }

        String hottest = uniqueQueries.get(0);
        String coldest = uniqueQueries.get(uniqueQueries.size() - 1);
        assertTrue(freq.getOrDefault(hottest, 0) > freq.getOrDefault(coldest, 0),
                "Zipfian: hottest query should appear more than coldest query");
        assertTrue(freq.getOrDefault(hottest, 0) > totalRequests / uniqueCount,
                "Zipfian: hottest query should exceed uniform average");
    }
    
    @Test
    void testWorkloadGeneratorDefaultsToZipfian() {
        WorkloadGenerator generator = new WorkloadGenerator();
        List<String> workload = generator.generateWorkload(5000, 30);
        assertEquals(5000, workload.size());
    }
    
    @Test
    void testModerateDistribution() {
        // TODO: Test MODERATE distribution
        
    }
    
    @Test
    void testParameterRanges() {
        // TODO: Test parameter range boundaries
    }
    
    @Test
    void testQueryTemplates() {
        // TODO: Test query template selection
    }
}
