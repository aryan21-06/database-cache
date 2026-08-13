package com.dbcache.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DistributionEngine {

     private final Random random = new Random();
    
    public List<String> applyModerateDistribution(List<String> uniqueQueries, int totalRequests) {
        List<String> workload = new ArrayList<>();
        List<WeightedString> weights = new ArrayList<>();
        int size = uniqueQueries.size();
        double totalWeight = (size*(size+1))/2;
        for(String a:uniqueQueries){
            weights.add(new WeightedString(a,size));
            size--;
        }
        for(int i=0;i<totalRequests;i++){
            workload.add(generateRandomQuery(weights, totalWeight));
        }
        return workload;
    }

    private String generateRandomQuery(List<WeightedString> strings,double totalWeight){
        double randomNum = random.nextDouble() * totalWeight;
        double currentSum = 0.0;
        for(WeightedString s:strings){
            currentSum += s.weight;
            if(currentSum>randomNum){
                return s.s;
            }

        }
        return strings.get(random.nextInt(strings.size())).s;
    }
}

class WeightedString{
    String s;
    double weight;

    public WeightedString(String s,double weight) {
        this.s = s;
        this.weight = weight;
    }
}
