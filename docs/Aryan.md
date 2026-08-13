# Request Handler & Benchmark Developer Guide - Database Query Cache System

## What You'll Build

You are responsible for two major components:

### 1. Request Handler

The orchestration layer that coordinates query execution between the UI, cache, and database.

### 2. Benchmark System

The complete benchmarking infrastructure including workload generation, execution, and metrics collection.

---

## Part 1: Request Handler

### Understanding Request Handler

**What it does:**

- Receives query requests from the UI
- Validates queries (only SELECT allowed)
- Checks cache first
- If cache miss, queries database
- Stores results in cache
- Returns response with metadata (cache hit/miss, timing, etc.)

**Why it exists:**

- Separates concerns (UI doesn't talk to cache/DB directly)
- Centralizes query validation
- Tracks timing and metadata
- Provides a clean API for the UI

### Your Responsibilities

You need to implement:

**File**: `src/main/java/com/dbcache/handler/RequestHandler.java`

This is the main class. It coordinates everything.

### Exact Contracts

#### RequestHandler Class

```java
public class RequestHandler {
  
    private final CacheEngine cache;
    private final DatabaseManager dbManager;
  
    /**
     * Constructor
     * 
     * @param dbManager Database manager for executing queries
     * @param config Cache configuration
     */
    public RequestHandler(DatabaseManager dbManager, CacheConfig config);
  
    /**
     * Execute a SQL query with caching.
     * 
     * @param sql The SQL SELECT query to execute
     * @return QueryResponse containing result and metadata
     */
    public QueryResponse executeQuery(String sql);
  
    /**
     * Get current cache statistics.
     * 
     * @return CacheStats object
     */
    public CacheStats getCacheStats();
  
    /**
     * Clear the cache.
     */
    public void clearCache();
  
    /**
     * Reconfigure cache settings.
     * 
     * @param config New cache configuration
     */
    public void configureCache(CacheConfig config);
}
```

#### Method Specifications

##### 1. `QueryResponse executeQuery(String sql)`

**Input:**

- `sql`: A SQL query string
- Can be null or empty

**Output:**

- Returns `QueryResponse` object containing:
  - `result`: QueryResult (null if error)
  - `cacheHit`: boolean (true if served from cache)
  - `responseTimeMs`: long (total time in milliseconds)
  - `databaseAccessed`: boolean (true if DB was queried)
  - `errorMessage`: String (null if success)

**Behavior:**

```java
// Scenario 1: Valid query, cache miss
QueryResponse response = handler.executeQuery("SELECT * FROM students WHERE id = 1");
// response.isSuccessful() = true
// response.isCacheHit() = false
// response.isDatabaseAccessed() = true
// response.getResponseTimeMs() = ~50ms
// response.getResult() = QueryResult object

// Scenario 2: Valid query, cache hit
handler.executeQuery("SELECT * FROM students WHERE id = 1"); // First call (miss)
QueryResponse response = handler.executeQuery("SELECT * FROM students WHERE id = 1"); // Second call
// response.isSuccessful() = true
// response.isCacheHit() = true
// response.isDatabaseAccessed() = false
// response.getResponseTimeMs() = ~1ms
// response.getResult() = QueryResult object

// Scenario 3: Invalid query (not SELECT)
QueryResponse response = handler.executeQuery("INSERT INTO students VALUES (...)");
// response.isSuccessful() = false
// response.getErrorMessage() = "Only SELECT queries are allowed"

// Scenario 4: Database error
QueryResponse response = handler.executeQuery("SELECT * FROM nonexistent_table");
// response.isSuccessful() = false
// response.getErrorMessage() = "Table 'dbcache.nonexistent_table' doesn't exist"

// Scenario 5: Null or empty query
QueryResponse response = handler.executeQuery(null);
// response.isSuccessful() = false
// response.getErrorMessage() = "Query cannot be empty"
```

**Implementation Logic:**

```java
public QueryResponse executeQuery(String sql) {
    long startTime = System.currentTimeMillis();
  
    // 1. Validate query
    if (!isValidSelectQuery(sql)) {
        return QueryResponse.error("Only SELECT queries are allowed");
    }
  
    // 2. Try cache
    QueryResult result = cache.get(sql);
    boolean cacheHit = (result != null);
    boolean databaseAccessed = false;
  
    // 3. If cache miss, query database
    if (!cacheHit) {
        try {
            result = dbManager.executeQuery(sql);
            cache.put(sql, result);
            databaseAccessed = true;
        } catch (SQLException e) {
            return QueryResponse.error("Database error: " + e.getMessage());
        }
    }
  
    // 4. Calculate response time
    long responseTime = System.currentTimeMillis() - startTime;
  
    // 5. Return response
    return QueryResponse.success(result, cacheHit, responseTime, databaseAccessed);
}
```

##### 2. `CacheStats getCacheStats()`

**Input:** None

**Output:**

- Returns `CacheStats` object from cache

**Behavior:**

```java
CacheStats stats = handler.getCacheStats();
// stats.getSize() = current cache size
// stats.getHits() = total cache hits
// stats.getMisses() = total cache misses
// stats.getHitRate() = hit rate (0.0 to 1.0)
```

##### 3. `void clearCache()`

**Input:** None

**Output:** None

**Behavior:**

```java
handler.clearCache();
// Cache is now empty
// All statistics reset to 0
```

##### 4. `void configureCache(CacheConfig config)`

**Input:**

- `config`: New cache configuration

**Output:** None

**Behavior:**

```java
CacheConfig newConfig = new CacheConfig(200, 120, EvictionPolicy.LRU);
handler.configureCache(newConfig);
// Cache capacity is now 200
// New TTL is 120 seconds
```

### Query Validation

**Rules:**

- Only SELECT queries allowed
- Reject INSERT, UPDATE, DELETE, DROP, ALTER, CREATE, TRUNCATE
- Simple check: query must start with "SELECT" (case-insensitive)

**Implementation:**

```java
private boolean isValidSelectQuery(String sql) {
    if (sql == null || sql.trim().isEmpty()) {
        return false;
    }
    String normalized = sql.trim().toLowerCase();
    return normalized.startsWith("select");
}
```

### Integration with Other Components

**UI calls you:**

```java
// From ManualQueryPanel
QueryResponse response = requestHandler.executeQuery(sql);

// From BenchmarkPanel
CacheStats stats = requestHandler.getCacheStats();
```

**You call cache:**

```java
QueryResult result = cache.get(sql);
cache.put(sql, result);
cache.clear();
CacheStats stats = cache.getStats();
```

**You call database:**

```java
QueryResult result = dbManager.executeQuery(sql);
```

---

## Part 2: Benchmark System

### Understanding the Benchmark System

**What it does:**

- Generates a workload of SQL queries
- Runs the workload WITHOUT cache (baseline)
- Runs the workload WITH cache
- Collects metrics for both runs
- Compares performance

**Why it exists:**

- Demonstrates cache effectiveness
- Measures performance improvement
- Provides data for analysis

### Your Responsibilities

You need to implement these files:

1. **BenchmarkRunner.java** - Orchestrates benchmark execution
2. **WorkloadGenerator.java** - Generates query workloads
3. **QueryBuilder.java** - Builds SQL queries from templates
4. **QueryTemplates.java** - Query template constants
5. **ParameterRanges.java** - Parameter range constants
6. **DistributionEngine.java** - Applies query distribution
7. **BenchmarkConfig.java** - Configuration (already done)
8. **BenchmarkMetrics.java** - Metrics collection (already done)
9. **BenchmarkResults.java** - Results wrapper (already done)

### Exact Contracts

#### BenchmarkRunner Class

```java
public class BenchmarkRunner {
  
    private final RequestHandler requestHandler;
    private final DatabaseManager dbManager;
  
    /**
     * Constructor
     * 
     * @param requestHandler Request handler for cached execution
     * @param dbManager Database manager for uncached execution
     */
    public BenchmarkRunner(RequestHandler requestHandler, DatabaseManager dbManager);
  
    /**
     * Run a complete benchmark.
     * 
     * @param config Benchmark configuration
     * @return BenchmarkResults containing metrics for both runs
     */
    public BenchmarkResults runBenchmark(BenchmarkConfig config);
}
```

##### `BenchmarkResults runBenchmark(BenchmarkConfig config)`

**Input:**

- `config`: Benchmark configuration with:
  - `totalRequests`: Total number of queries to execute
  - `uniqueQueries`: Number of unique queries in workload
  - `cacheCapacity`: Cache capacity for the run
  - `ttlSeconds`: TTL for cache entries

**Output:**

- Returns `BenchmarkResults` containing:
  - `noCacheMetrics`: Metrics from run without cache
  - `withCacheMetrics`: Metrics from run with cache

**Behavior:**

```java
BenchmarkConfig config = new BenchmarkConfig(10000, 1000, 100, 60);
BenchmarkResults results = benchmarkRunner.runBenchmark(config);

BenchmarkMetrics noCache = results.getNoCacheMetrics();
// noCache.getTotalRequests() = 10000
// noCache.getDbQueries() = 10000 (all queries hit DB)
// noCache.getCacheHits() = 0
// noCache.getTotalTimeMs() = ~8000ms

BenchmarkMetrics withCache = results.getWithCacheMetrics();
// withCache.getTotalRequests() = 10000
// withCache.getDbQueries() = ~1000 (only unique queries)
// withCache.getCacheHits() = ~9000
// withCache.getTotalTimeMs() = ~2000ms
```

**Implementation Logic:**

```java
public BenchmarkResults runBenchmark(BenchmarkConfig config) {
    // 1. Generate workload
    WorkloadGenerator generator = new WorkloadGenerator();
    List<String> workload = generator.generateWorkload(
        config.getTotalRequests(),
        config.getUniqueQueries()
    );
  
    // 2. Run WITHOUT cache
    BenchmarkMetrics noCacheMetrics = runWithoutCache(workload);
  
    // 3. Clear cache and reconfigure
    requestHandler.clearCache();
    requestHandler.configureCache(new CacheConfig(
        config.getCacheCapacity(),
        config.getTtlSeconds(),
        EvictionPolicy.LRU
    ));
  
    // 4. Run WITH cache
    BenchmarkMetrics withCacheMetrics = runWithCache(workload);
  
    // 5. Return results
    return new BenchmarkResults(noCacheMetrics, withCacheMetrics);
}

private BenchmarkMetrics runWithoutCache(List<String> workload) {
    long startTime = System.currentTimeMillis();
    int dbQueries = 0;
  
    for (String sql : workload) {
        try {
            dbManager.executeQuery(sql);
            dbQueries++;
        } catch (SQLException e) {
            // Log error but continue
        }
    }
  
    long totalTime = System.currentTimeMillis() - startTime;
    double avgLatency = (double) totalTime / workload.size();
  
    return new BenchmarkMetrics(
        workload.size(),
        dbQueries,
        0,  // no cache hits
        workload.size(),  // all misses
        0.0,  // 0% hit rate
        totalTime,
        avgLatency
    );
}

private BenchmarkMetrics runWithCache(List<String> workload) {
    long startTime = System.currentTimeMillis();
    int dbQueries = 0;
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
```

---

#### WorkloadGenerator Class

```java
public class WorkloadGenerator {
  
    private final QueryBuilder queryBuilder;
    private final DistributionEngine distributionEngine;
  
    public WorkloadGenerator();
  
    /**
     * Generate a complete workload.
     * 
     * @param totalRequests Total number of queries to generate
     * @param uniqueQueries Number of unique queries in the pool
     * @return List of SQL queries ready for execution
     */
    public List<String> generateWorkload(int totalRequests, int uniqueQueries);
}
```

##### `List<String> generateWorkload(int totalRequests, int uniqueQueries)`

**Input:**

- `totalRequests`: Total number of queries (e.g., 10000)
- `uniqueQueries`: Number of unique queries (e.g., 1000)

**Output:**

- Returns `List<String>` of SQL queries
- List size = `totalRequests`
- Contains repetitions based on MODERATE distribution

**Behavior:**

```java
WorkloadGenerator generator = new WorkloadGenerator();
List<String> workload = generator.generateWorkload(10000, 1000);

// workload.size() = 10000
// workload contains ~1000 unique queries
// Some queries appear more frequently (MODERATE distribution)
// Example:
// workload.get(0) = "SELECT * FROM students WHERE id = 5"
// workload.get(1) = "SELECT * FROM courses WHERE credits > 3"
// workload.get(2) = "SELECT * FROM students WHERE id = 5"  // repeated
```

**Implementation Logic:**

```java
public List<String> generateWorkload(int totalRequests, int uniqueQueries) {
    // 1. Generate unique queries
    List<String> uniqueQueryPool = queryBuilder.generateUniqueQueries(uniqueQueries);
  
    // 2. Apply distribution
    List<String> workload = distributionEngine.applyModerateDistribution(
        uniqueQueryPool,
        totalRequests
    );
  
    return workload;
}
```

---

#### QueryBuilder Class

```java
public class QueryBuilder {
  
    public QueryBuilder();
  
    /**
     * Generate a list of unique queries.
     * 
     * @param count Number of unique queries to generate
     * @return List of unique SQL queries
     */
    public List<String> generateUniqueQueries(int count);
}
```

##### `List<String> generateUniqueQueries(int count)`

**Input:**

- `count`: Number of unique queries to generate

**Output:**

- Returns `List<String>` of unique SQL queries
- Each query is different
- Queries cover all tables (students, courses, enrollments, departments)
- Mix of simple filters, aggregates, and joins

**Behavior:**

```java
QueryBuilder builder = new QueryBuilder();
List<String> queries = builder.generateUniqueQueries(10);

// queries might contain:
// "SELECT * FROM students WHERE id = 5"
// "SELECT * FROM students WHERE department_id = 2"
// "SELECT * FROM courses WHERE credits > 3"
// "SELECT COUNT(*) FROM students WHERE department_id = 1"
// "SELECT s.name, c.title FROM students s JOIN enrollments e ON s.id = e.student_id JOIN courses c ON e.course_id = c.id WHERE s.department_id = 3"
// ... (10 unique queries)
```

**Implementation Logic:**

```java
public List<String> generateUniqueQueries(int count) {
    List<String> queries = new ArrayList<>();
  
    for (int i = 0; i < count; i++) {
        queries.add(generateRandomQuery());
    }
  
    return queries;
}

private String generateRandomQuery() {
    // Select random template category
    int category = random.nextInt(5);
  
    return switch (category) {
        case 0 -> generateStudentQuery();
        case 1 -> generateCourseQuery();
        case 2 -> generateEnrollmentQuery();
        case 3 -> generateDepartmentQuery();
        case 4 -> generateJoinQuery();
        default -> generateStudentQuery();
    };
}

private String generateStudentQuery() {
    String template = randomFrom(QueryTemplates.STUDENT_TEMPLATES);
    return fillParameters(template);
}

private String fillParameters(String template) {
    String result = template;
  
    while (result.contains("?")) {
        Object value = generateRandomParameter(template);
        result = result.replaceFirst("\\?", String.valueOf(value));
    }
  
    return result;
}

private Object generateRandomParameter(String template) {
    if (template.contains("student_id")) {
        return randomInt(ParameterRanges.STUDENT_ID_MIN, ParameterRanges.STUDENT_ID_MAX);
    } else if (template.contains("department_id")) {
        return randomInt(ParameterRanges.DEPARTMENT_ID_MIN, ParameterRanges.DEPARTMENT_ID_MAX);
    } else if (template.contains("course_id")) {
        return randomInt(ParameterRanges.COURSE_ID_MIN, ParameterRanges.COURSE_ID_MAX);
    }
    // ... more cases
}
```

---

#### QueryTemplates Class

This is a constants class. Already implemented in skeleton.

```java
public class QueryTemplates {
  
    // Students table queries
    public static final String[] STUDENT_TEMPLATES = {
        "SELECT * FROM students WHERE id = ?",
        "SELECT * FROM students WHERE department_id = ?",
        "SELECT * FROM students WHERE year = ?",
        "SELECT * FROM students WHERE gpa > ?",
        "SELECT name, email FROM students WHERE department_id = ? AND year = ?",
        "SELECT COUNT(*) FROM students WHERE department_id = ?",
        "SELECT AVG(gpa) FROM students WHERE department_id = ?",
    };
  
    // Courses table queries
    public static final String[] COURSE_TEMPLATES = {
        "SELECT * FROM courses WHERE id = ?",
        "SELECT * FROM courses WHERE credits > ?",
        "SELECT * FROM courses WHERE department_id = ?",
        "SELECT title, credits FROM courses WHERE credits >= ?",
        "SELECT COUNT(*) FROM courses WHERE department_id = ?",
    };
  
    // Enrollments table queries
    public static final String[] ENROLLMENT_TEMPLATES = {
        "SELECT * FROM enrollments WHERE student_id = ?",
        "SELECT * FROM enrollments WHERE course_id = ?",
        "SELECT * FROM enrollments WHERE semester = ?",
        "SELECT COUNT(*) FROM enrollments WHERE student_id = ?",
        "SELECT * FROM enrollments WHERE grade = ?",
    };
  
    // Departments table queries
    public static final String[] DEPARTMENT_TEMPLATES = {
        "SELECT * FROM departments WHERE id = ?",
        "SELECT * FROM departments WHERE name = ?",
    };
  
    // Join queries
    public static final String[] JOIN_TEMPLATES = {
        "SELECT s.name, c.title FROM students s JOIN enrollments e ON s.id = e.student_id JOIN courses c ON e.course_id = c.id WHERE s.department_id = ?",
        "SELECT s.name, e.grade FROM students s JOIN enrollments e ON s.id = e.student_id WHERE e.course_id = ?",
    };
}
```

---

#### ParameterRanges Class

This is a constants class. Already implemented in skeleton.

```java
public class ParameterRanges {
  
    // Student parameters
    public static final int STUDENT_ID_MIN = 1;
    public static final int STUDENT_ID_MAX = 1000;
    public static final int DEPARTMENT_ID_MIN = 1;
    public static final int DEPARTMENT_ID_MAX = 5;
    public static final int YEAR_MIN = 1;
    public static final int YEAR_MAX = 4;
    public static final double GPA_MIN = 0.0;
    public static final double GPA_MAX = 4.0;
  
    // Course parameters
    public static final int COURSE_ID_MIN = 1;
    public static final int COURSE_ID_MAX = 100;
    public static final int CREDITS_MIN = 1;
    public static final int CREDITS_MAX = 6;
  
    // Semester values
    public static final String[] SEMESTERS = {
        "Fall 2023", "Spring 2024", "Fall 2024", "Spring 2025"
    };
  
    // Grade values
    public static final String[] GRADES = {
        "A", "B", "C", "D", "F"
    };
  
    // Department names
    public static final String[] DEPARTMENT_NAMES = {
        "Computer Science", "Mathematics", "Physics", "Chemistry", "Biology"
    };
}
```

---

#### DistributionEngine Class

```java
public class DistributionEngine {
  
    public DistributionEngine();
  
    /**
     * Apply MODERATE distribution to select queries from the unique query pool.
     * 
     * MODERATE distribution uses linearly decreasing weights:
     * - Query 0: weight = N (most frequent)
     * - Query 1: weight = N-1
     * - Query 2: weight = N-2
     * - ...
     * - Query N-1: weight = 1 (least frequent)
     * 
     * @param uniqueQueries Pool of unique queries
     * @param totalRequests Total number of requests to generate
     * @return List of queries with repetitions based on distribution
     */
    public List<String> applyModerateDistribution(
        List<String> uniqueQueries,
        int totalRequests
    );
}
```

##### `List<String> applyModerateDistribution(List<String> uniqueQueries, int totalRequests)`

**Input:**

- `uniqueQueries`: List of unique queries (e.g., 1000 queries)
- `totalRequests`: Total number of requests to generate (e.g., 10000)

**Output:**

- Returns `List<String>` of queries
- List size = `totalRequests`
- Earlier queries in `uniqueQueries` appear more frequently

**Behavior:**

```java
DistributionEngine engine = new DistributionEngine();

List<String> uniqueQueries = Arrays.asList("q1", "q2", "q3", "q4", "q5");
List<String> workload = engine.applyModerateDistribution(uniqueQueries, 100);

// workload.size() = 100
// "q1" appears ~33 times (weight 5)
// "q2" appears ~27 times (weight 4)
// "q3" appears ~20 times (weight 3)
// "q4" appears ~13 times (weight 2)
// "q5" appears ~7 times (weight 1)
```

**Implementation Logic:**

```java
public List<String> applyModerateDistribution(
    List<String> uniqueQueries,
    int totalRequests
) {
    List<String> workload = new ArrayList<>();
    int n = uniqueQueries.size();
  
    // Create weight array: [n, n-1, n-2, ..., 1]
    int[] weights = new int[n];
    int totalWeight = 0;
    for (int i = 0; i < n; i++) {
        weights[i] = n - i;
        totalWeight += weights[i];
    }
  
    // Select queries based on weights
    for (int i = 0; i < totalRequests; i++) {
        int randomWeight = random.nextInt(totalWeight);
        int cumulativeWeight = 0;
  
        for (int j = 0; j < n; j++) {
            cumulativeWeight += weights[j];
            if (randomWeight < cumulativeWeight) {
                workload.add(uniqueQueries.get(j));
                break;
            }
        }
    }
  
    return workload;
}
```

---

## Integration Points

### How UI Uses Your Components

**From ManualQueryPanel:**

```java
QueryResponse response = requestHandler.executeQuery(sql);
```

**From BenchmarkPanel:**

```java
BenchmarkResults results = benchmarkRunner.runBenchmark(config);
```

### How You Use Other Components

**You use CacheEngine:**

```java
// In RequestHandler
QueryResult result = cache.get(sql);
cache.put(sql, result);
cache.clear();
CacheStats stats = cache.getStats();
```

**You use DatabaseManager:**

```java
// In RequestHandler (on cache miss)
QueryResult result = dbManager.executeQuery(sql);

// In BenchmarkRunner (for uncached run)
dbManager.executeQuery(sql);
```

---

## Testing Requirements

### RequestHandler Tests

**File**: `src/test/java/com/dbcache/handler/RequestHandlerTest.java`

**Test cases:**

1. Valid SELECT query - cache miss
2. Valid SELECT query - cache hit
3. Invalid query (INSERT) - should return error
4. Invalid query (UPDATE) - should return error
5. Null query - should return error
6. Empty query - should return error
7. Database error - should return error response
8. Cache statistics tracking
9. Clear cache functionality
10. Reconfigure cache

### Benchmark Tests

**File**: `src/test/java/com/dbcache/benchmark/BenchmarkTest.java`

**Test cases:**

1. WorkloadGenerator generates correct number of queries
2. WorkloadGenerator generates unique queries
3. DistributionEngine applies MODERATE distribution correctly
4. QueryBuilder generates valid SQL
5. QueryBuilder fills parameters correctly
6. BenchmarkRunner runs without cache
7. BenchmarkRunner runs with cache
8. BenchmarkMetrics calculated correctly
9. Full benchmark execution

### Performance Tests

**Test with realistic workload:**

```java
@Test
void testBenchmarkPerformance() {
    BenchmarkConfig config = new BenchmarkConfig(10000, 1000, 100, 60);
    BenchmarkResults results = benchmarkRunner.runBenchmark(config);
  
    // Verify metrics are reasonable
    assertTrue(results.getWithCacheMetrics().getTotalTimeMs() < 
               results.getNoCacheMetrics().getTotalTimeMs());
    assertTrue(results.getWithCacheMetrics().getCacheHits() > 0);
}
```

---

## Edge Cases to Handle

### RequestHandler

1. **Null SQL**: Return error response
2. **Empty SQL**: Return error response
3. **SQL with only whitespace**: Return error response
4. **SQL with leading/trailing spaces**: Should work (trim it)
5. **SQL with mixed case**: Should work (validate case-insensitive)
6. **Database connection lost**: Return error response
7. **Cache throws exception**: Handle gracefully

### Benchmark

1. **totalRequests = 0**: Return empty metrics
2. **uniqueQueries = 0**: Return error or empty workload
3. **uniqueQueries > totalRequests**: Handle gracefully
4. **cacheCapacity = 0**: Should still work (no caching)
5. **ttlSeconds = 0**: Entries expire immediately
6. **Database error during benchmark**: Continue with remaining queries

---

## Common Mistakes to Avoid

### 1. Not Validating Queries

```java
// WRONG: No validation
public QueryResponse executeQuery(String sql) {
    QueryResult result = cache.get(sql);
    // ...
}

// RIGHT: Validate first
public QueryResponse executeQuery(String sql) {
    if (!isValidSelectQuery(sql)) {
        return QueryResponse.error("Only SELECT queries are allowed");
    }
    // ...
}
```

### 2. Not Measuring Time Correctly

```java
// WRONG: Measuring only cache time
long startTime = System.currentTimeMillis();
QueryResult result = cache.get(sql);
long time = System.currentTimeMillis() - startTime;

// RIGHT: Measure total time
long startTime = System.currentTimeMillis();
// ... all operations ...
long time = System.currentTimeMillis() - startTime;
```

### 3. Not Clearing Cache Between Benchmark Runs

```java
// WRONG: Cache has data from previous run
BenchmarkMetrics noCache = runWithoutCache(workload);
BenchmarkMetrics withCache = runWithCache(workload);

// RIGHT: Clear cache before cached run
BenchmarkMetrics noCache = runWithoutCache(workload);
requestHandler.clearCache();
BenchmarkMetrics withCache = runWithCache(workload);
```

### 4. Generating Duplicate Unique Queries

```java
// WRONG: Might generate duplicates
for (int i = 0; i < count; i++) {
    queries.add(generateRandomQuery()); // Could be duplicate
}

// RIGHT: Ensure uniqueness
Set<String> uniqueSet = new HashSet<>();
while (uniqueSet.size() < count) {
    uniqueSet.add(generateRandomQuery());
}
queries.addAll(uniqueSet);
```

---

## Quick Reference

### RequestHandler Methods

```java
QueryResponse executeQuery(String sql);
CacheStats getCacheStats();
void clearCache();
void configureCache(CacheConfig config);
```

### BenchmarkRunner Methods

```java
BenchmarkResults runBenchmark(BenchmarkConfig config);
```

### WorkloadGenerator Methods

```java
List<String> generateWorkload(int totalRequests, int uniqueQueries);
```

### QueryBuilder Methods

```java
List<String> generateUniqueQueries(int count);
```

### DistributionEngine Methods

```java
List<String> applyModerateDistribution(List<String> uniqueQueries, int totalRequests);
```

---

## Summary

**What you need to implement:**

1. ✅ RequestHandler with query validation and cache coordination
2. ✅ BenchmarkRunner with uncached and cached execution
3. ✅ WorkloadGenerator with query generation
4. ✅ QueryBuilder with template-based query generation
5. ✅ DistributionEngine with MODERATE distribution
6. ✅ All unit tests passing
7. ✅ Integration with UI and other components

**What's already implemented:**

- QueryResponse, QueryResult, QueryValidator (model classes)
- BenchmarkConfig, BenchmarkMetrics, BenchmarkResults (data classes)
- QueryTemplates, ParameterRanges (constants)

**What you need to deliver:**

- Working RequestHandler implementation
- Working Benchmark system implementation
- All unit tests passing
- Integration with UI

Good luck! You've got this.
