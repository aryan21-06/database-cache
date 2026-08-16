package com.dbcache.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZipfianDistribution {

    private static final double DEFAULT_SKEW = 0.99;

    private final double skew;
    private final Random random;

    public ZipfianDistribution() {
        this(DEFAULT_SKEW);
    }

    public ZipfianDistribution(double skew) {
        this.skew = skew;
        this.random = new Random();
    }

    public List<String> applyZipfianDistribution(List<String> uniqueQueries, int totalRequests) {
        int n = uniqueQueries.size();
        if (n == 0) return new ArrayList<>();

        double[] cumulative = buildCumulativeDistribution(n);
        List<String> workload = new ArrayList<>(totalRequests);

        for (int i = 0; i < totalRequests; i++) {
            int index = sample(cumulative);
            workload.add(uniqueQueries.get(index));
        }

        return workload;
    }

    private double[] buildCumulativeDistribution(int n) {
        double[] cumulative = new double[n];
        double totalWeight = 0.0;

        for (int i = 0; i < n; i++) {
            double weight = 1.0 / Math.pow(i + 1, skew);
            totalWeight += weight;
            cumulative[i] = totalWeight;
        }

        for (int i = 0; i < n; i++) {
            cumulative[i] /= totalWeight;
        }

        return cumulative;
    }

    private int sample(double[] cumulative) {
        double r = random.nextDouble();
        int lo = 0;
        int hi = cumulative.length - 1;

        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (cumulative[mid] < r) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }

        return lo;
    }
}
