# Database Query Cache System - Architecture Document

## 1. System Overview

### 1.1 Purpose
A Java desktop application demonstrating how an in-memory cache reduces database query load and improves response time. The system allows users to execute SQL queries, observe cache behavior (hits/misses), and run benchmarks to measure performance improvements.

### 1.2 Core Concept
```
User Query → Request Handler → Cache Engine
                                    ↓
                              ┌─────┴─────┐
                              │           │
                          Cache HIT   Cache MISS
                              │           │
                           Return     Database Manager
                           Result          ↓
                              │          JDBC
                              │           ↓
                              │       Database
                              │           ↓
                              │     ResultSet
                              │           ↓
                              │  Convert to QueryResult
                              │           ↓
                              │     Store in Cache
                              │           ↓
                              └─────┬─────┘
                                    ↓
                              Return Result
```

### 1.3 Technology Stack
- **Language**: Java 17+
- **UI**: Java Swing
- **Database**: MySQL
- **JDBC**: MySQL Connector/J
- **Build Tool**: Maven
- **Testing**: JUnit 5

### 1.4 Execution Model
- **Sequential execution** (V1): All queries execute one at a time
- **Single-threaded**: No concurrency concerns in V1
- **Blocking UI**: UI will be unresponsive during long operations (acceptable for V1)

---

## 2. Component Architecture

### 2.1 High-Level Components

```
┌──────────────────────────────────────────────┐
│              Java Application                 │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │          UI Layer (Swing)              │  │
│  │  - MainFrame (tabbed container)        │  │
│  │  - ManualQueryDialog                   │  │
│  │  - BenchmarkDialog                     │  │
│  │  - CacheStatsDialog                    │  │
│  └────────────────┬───────────────────────┘  │
│                   │                          │
│  ┌────────────────┴───────────────────────┐  │
│  │         Request Handler                │  │
│  │  - Validates queries                   │  │
│  │  - Coordinates cache + DB              │  │
│  └────────────────┬───────────────────────┘  │
│                   │                          │
│  ┌────────────────┴───────────────────────┐  │
│  │           Cache Engine                 │  │
│  │  - Cache lookup/insert                 │  │
│  │  - LRU eviction                        │  │
│  │  - TTL expiration                      │  │
│  │  - Statistics tracking                 │  │
│  └────────────────┬───────────────────────┘  │
│                   │                          │
│  ┌────────────────┴───────────────────────┐  │
│  │        Database Manager                │  │
│  │  - JDBC connection management          │  │
│  │  - Query execution                     │  │
│  │  - ResultSet → QueryResult conversion  │  │
│  └────────────────┬───────────────────────┘  │
└───────────────────┼──────────────────────────┘
                    │
          ┌─────────┴─────────┐
          │   MySQL Database  │
          └───────────────────┘
```

### 2.2 Component Responsibilities

| Component | Responsibility |
|-----------|---------------|
| **UI Layer** | User interaction, display results, collect input |
| **Request Handler** | Orchestrate query execution, validate SQL, coordinate cache/DB |
| **Cache Engine** | Store/retrieve cached results, manage capacity, TTL, eviction |
| **Database Manager** | Execute queries via JDBC, convert ResultSet to QueryResult |

---

## 3. Interface Contracts

### 3.1 Request Handler Interface

The Request Handler is the main entry point for the UI layer.

```java
public class RequestHandler {
    
    /**
     * Execute a SQL query with caching.
     * 
     * @param sql The SQL SELECT query to execute
     * @return QueryResponse containing result and metadata
     */
    public QueryResponse executeQuery(String sql);
    
    /**
     * Get current cache statistics.
     */
    public CacheStats getCacheStats();
    
    /**
     * Clear the cache.
     */
    public void clearCache();
    
    /**
     * Configure cache settings.
     */
    public void configureCache(CacheConfig config);
}
```

#### QueryResponse Model

```java
public class QueryResponse {
    // The query result (null if error)
    private QueryResult result;
    
    // Whether this was a cache hit
    private boolean cacheHit;
    
    // Response time in milliseconds
    private long responseTimeMs;
    
    // Whether database was accessed
    private boolean databaseAccessed;
    
    // Error message (null if success)
    private String errorMessage;
    
    // Getters/setters...
}
```

#### QueryResult Model

```java
public class QueryResult {
    // Column names
    private List<String> columns;
    
    // Row data (each row is a list of values)
    private List<List<Object>> rows;
    
    // Number of rows
    private int rowCount;
    
    // Getters/setters...
}
```

#### CacheStats Model

```java
public class CacheStats {
    private int size;              // Current number of entries
    private int capacity;          // Maximum capacity
    private long hits;             // Total cache hits
    private long misses;           // Total cache misses
    private long evictions;        // Total evictions
    private double hitRate;        // hits / (hits + misses)
    
    // Getters...
}
```

#### CacheConfig Model

```java
public class CacheConfig {
    private int capacity;          // Max entries (e.g., 100)
    private long ttlSeconds;       // Time-to-live in seconds (e.g., 60)
    private EvictionPolicy policy; // LRU or FIFO
    
    // Constructor, getters...
}

public enum EvictionPolicy {
    LRU,
    FIFO
}
```

### 3.2 Usage Example for UI Layer

```java
// Initialize (once at startup)
RequestHandler handler = new RequestHandler();
handler.configureCache(new CacheConfig(100, 60, EvictionPolicy.LRU));

// Execute a query
QueryResponse response = handler.executeQuery("SELECT * FROM students WHERE id = 10");

if (response.isSuccessful()) {
    // Display results
    QueryResult result = response.getResult();
    List<String> columns = result.getColumns();
    List<List<Object>> rows = result.getRows();
    
    // Show metadata
    boolean wasCacheHit = response.isCacheHit();
    long timeMs = response.getResponseTimeMs();
    
    // Update JTable with results
}

// Get cache stats
CacheStats stats = handler.getCacheStats();
System.out.println("Hit Rate: " + stats.getHitRate());

// Clear cache
handler.clearCache();
```

---

## 4. Cache Engine Design

### 4.1 Cache Engine Interface

```java
public interface CacheEngine {
    
    /**
     * Retrieve a cached query result.
     * 
     * @param sql The SQL query (will be normalized)
     * @return The cached QueryResult, or null if not found/expired
     */
    QueryResult get(String sql);
    
    /**
     * Store a query result in the cache.
     * 
     * @param sql The SQL query
     * @param result The query result to cache
     */
    void put(String sql, QueryResult result);
    
    /**
     * Clear all entries from the cache.
     */
    void clear();
    
    /**
     * Get current cache statistics.
     */
    CacheStats getStats();
    
    /**
     * Configure cache settings.
     */
    void configure(CacheConfig config);
}
```

### 4.2 LRU Cache Implementation

**Data Structure**: `LinkedHashMap` with access-order enabled

```java
public class LRUCache implements CacheEngine {
    
    private final Map<String, CacheEntry> cache;
    private int capacity;
    private long ttlMillis;
    
    // Statistics
    private long hits = 0;
    private long misses = 0;
    private long evictions = 0;
    
    public LRUCache(int capacity, long ttlSeconds) {
        this.capacity = capacity;
        this.ttlMillis = ttlSeconds * 1000;
        
        // LinkedHashMap with access-order = true for LRU behavior
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                boolean shouldRemove = size() > capacity;
                if (shouldRemove) {
                    evictions++;
                }
                return shouldRemove;
            }
        };
    }
    
    @Override
    public QueryResult get(String sql) {
        String key = normalizeQuery(sql);
        CacheEntry entry = cache.get(key);
        
        if (entry == null) {
            misses++;
            return null;
        }
        
        // Check TTL
        if (isExpired(entry)) {
            cache.remove(key);
            misses++;
            return null;
        }
        
        // Update access metadata
        entry.recordAccess();
        hits++;
        return entry.getResult();
    }
    
    @Override
    public void put(String sql, QueryResult result) {
        String key = normalizeQuery(sql);
        CacheEntry entry = new CacheEntry(result);
        cache.put(key, entry);
    }
    
    private boolean isExpired(CacheEntry entry) {
        return System.currentTimeMillis() - entry.getCreatedAt() > ttlMillis;
    }
    
    private String normalizeQuery(String sql) {
        // Lowercase, trim, remove trailing semicolons, normalize whitespace
        return sql.toLowerCase()
                  .trim()
                  .replaceAll(";\\s*$", "")
                  .replaceAll("\\s+", " ");
    }
}
```

### 4.3 Cache Entry

```java
public class CacheEntry {
    private final QueryResult result;
    private final long createdAt;
    private long lastAccessedAt;
    private int accessCount;
    
    public CacheEntry(QueryResult result) {
        this.result = result;
        this.createdAt = System.currentTimeMillis();
        this.lastAccessedAt = createdAt;
        this.accessCount = 1;
    }
    
    public void recordAccess() {
        this.lastAccessedAt = System.currentTimeMillis();
        this.accessCount++;
    }
    
    // Getters...
}
```

### 4.4 Cache Key Normalization

**Goal**: Ensure equivalent queries map to the same cache key.

**Normalization Steps**:
1. Convert to lowercase
2. Trim leading/trailing whitespace
3. Remove trailing semicolons
4. Collapse multiple spaces into single space

**Examples**:
```
Input:  "SELECT * FROM students WHERE id = 10;"
Output: "select * from students where id = 10"

Input:  "  select   *   from   students   where   id=10  "
Output: "select * from students where id=10"
```

**Limitation**: Does not handle semantic equivalence (e.g., `WHERE id=10` vs `WHERE 10=id`). This is acceptable for V1.

### 4.5 TTL Strategy

**Approach**: Lazy expiration (check on access)

- When `get()` is called, check if entry is expired
- If expired, remove from cache and return null (cache miss)
- No background cleanup thread needed

**Why**: Simpler implementation, no threading required for V1.

---

## 5. Database Manager Design

### 5.1 Database Manager Interface

```java
public class DatabaseManager {
    
    private final Connection connection;
    
    public DatabaseManager(String url, String username, String password) 
            throws SQLException {
        this.connection = DriverManager.getConnection(url, username, password);
    }
    
    /**
     * Execute a SQL query and return materialized result.
     * 
     * @param sql The SQL query to execute
     * @return QueryResult containing column names and row data
     */
    public QueryResult executeQuery(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            return convertResultSet(rs);
        }
    }
    
    /**
     * Convert JDBC ResultSet to QueryResult.
     */
    private QueryResult convertResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        // Extract column names
        List<String> columns = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            columns.add(metaData.getColumnName(i));
        }
        
        // Extract rows
        List<List<Object>> rows = new ArrayList<>();
        while (rs.next()) {
            List<Object> row = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                row.add(rs.getObject(i));
            }
            rows.add(row);
        }
        
        return new QueryResult(columns, rows);
    }
    
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
```

### 5.2 QueryResult Model

```java
public class QueryResult {
    private final List<String> columns;
    private final List<List<Object>> rows;
    
    public QueryResult(List<String> columns, List<List<Object>> rows) {
        this.columns = columns;
        this.rows = rows;
    }
    
    public List<String> getColumns() {
        return columns;
    }
    
    public List<List<Object>> getRows() {
        return rows;
    }
    
    public int getRowCount() {
        return rows.size();
    }
    
    public int getColumnCount() {
        return columns.size();
    }
}
```

**Important**: QueryResult is a plain Java object, not a JDBC ResultSet. This allows it to be stored in the cache independently of database connections.

---

## 6. Request Handler Implementation

### 6.1 Request Handler

```java
public class RequestHandler {
    
    private final CacheEngine cache;
    private final DatabaseManager dbManager;
    
    public RequestHandler(DatabaseManager dbManager, CacheConfig config) {
        this.dbManager = dbManager;
        this.cache = new LRUCache(config.getCapacity(), config.getTtlSeconds());
    }
    
    public QueryResponse executeQuery(String sql) {
        long startTime = System.currentTimeMillis();
        
        // Validate query
        if (!isValidSelectQuery(sql)) {
            return QueryResponse.error("Only SELECT queries are allowed");
        }
        
        // Try cache first
        QueryResult result = cache.get(sql);
        boolean cacheHit = (result != null);
        boolean dbAccessed = false;
        
        // Cache miss - query database
        if (!cacheHit) {
            try {
                result = dbManager.executeQuery(sql);
                cache.put(sql, result);
                dbAccessed = true;
            } catch (SQLException e) {
                return QueryResponse.error("Database error: " + e.getMessage());
            }
        }
        
        long responseTime = System.currentTimeMillis() - startTime;
        
        return QueryResponse.success(result, cacheHit, responseTime, dbAccessed);
    }
    
    private boolean isValidSelectQuery(String sql) {
        String normalized = sql.trim().toLowerCase();
        return normalized.startsWith("select");
    }
    
    public CacheStats getCacheStats() {
        return cache.getStats();
    }
    
    public void clearCache() {
        cache.clear();
    }
    
    public void configureCache(CacheConfig config) {
        cache.configure(config);
    }
}
```

### 6.2 Query Validation

**V1 Rules**:
- Only `SELECT` queries allowed
- Reject `INSERT`, `UPDATE`, `DELETE`, `DROP`, etc.
- Simple check: query must start with "SELECT" (case-insensitive)

**Future Enhancement**: More sophisticated validation (e.g., reject queries with non-deterministic functions like `NOW()`, `RAND()`).

---

## 7. Data Flow Diagrams

### 7.1 Query Execution Flow (Cache Hit)

```
UI Layer
   │
   │ executeQuery("SELECT * FROM students WHERE id = 10")
   ↓
Request Handler
   │
   │ cache.get(sql)
   ↓
Cache Engine
   │
   │ normalize("SELECT * FROM students WHERE id = 10")
   │ → "select * from students where id = 10"
   │
   │ lookup in map
   │ → found, not expired
   │
   │ return QueryResult
   ↓
Request Handler
   │
   │ return QueryResponse(cacheHit=true, dbAccessed=false)
   ↓
UI Layer
   │
   │ Display results, show "CACHE HIT", response time
```

### 7.2 Query Execution Flow (Cache Miss)

```
UI Layer
   │
   │ executeQuery("SELECT * FROM students WHERE id = 10")
   ↓
Request Handler
   │
   │ cache.get(sql)
   ↓
Cache Engine
   │
   │ lookup in map
   │ → not found
   │
   │ return null
   ↓
Request Handler
   │
   │ dbManager.executeQuery(sql)
   ↓
Database Manager
   │
   │ JDBC query → MySQL
   │ ← ResultSet
   │
   │ convertResultSet()
   │ → QueryResult
   ↓
Request Handler
   │
   │ cache.put(sql, result)
   ↓
Cache Engine
   │
   │ store in map
   │ → check capacity, evict if needed
   ↓
Request Handler
   │
   │ return QueryResponse(cacheHit=false, dbAccessed=true)
   ↓
UI Layer
   │
   │ Display results, show "CACHE MISS", response time
```

### 7.3 Benchmark Execution Flow

```
UI Layer (BenchmarkDialog)
   │
   │ configure: 10000 requests, 1000 unique queries
   │
   │ runBenchmark()
   ↓
Benchmark Runner
   │
   │ 1. Generate workload
   │    → WorkloadGenerator creates List<String> queries
   │    → Uses MODERATE distribution (weighted, linearly decreasing)
   │
   │ 2. Run WITHOUT cache
   │    → Disable cache
   │    → For each query: dbManager.executeQuery()
   │    → Measure total time, count DB queries
   │
   │ 3. Clear cache
   │
   │ 4. Run WITH cache
   │    → Enable cache
   │    → For each query: requestHandler.executeQuery()
   │    → Measure total time, track hits/misses
   │
   │ 5. Calculate metrics
   │    → Speedup, hit rate, DB load reduction
   │
   │ return BenchmarkResults
   ↓
UI Layer
   │
   │ Display comparison table
```

---

## 8. Benchmark System Design

### 8.1 Workload Generator Architecture

```
┌─────────────────────────────────────────────────────────┐
│                 Workload Generator                       │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │  1. Query Templates (Hardcoded)                  │  │
│  │     - 20+ template patterns                      │  │
│  │     - Cover all 4 tables                         │  │
│  │     - Mix of simple filters, joins, aggregates   │  │
│  └──────────────────────────────────────────────────┘  │
│                          ↓                              │
│  ┌──────────────────────────────────────────────────┐  │
│  │  2. Parameter Ranges (Hardcoded)                 │  │
│  │     - student_id: 1-1000                         │  │
│  │     - department_id: 1-5                         │  │
│  │     - course_id: 1-100                           │  │
│  │     - credits: 1-6                               │  │
│  │     - year: 1-4                                  │  │
│  └──────────────────────────────────────────────────┘  │
│                          ↓                              │
│  ┌──────────────────────────────────────────────────┐  │
│  │  3. Query Builder                                │  │
│  │     - Takes template + random params → SQL       │  │
│  │     - Generates N unique queries                 │  │
│  └──────────────────────────────────────────────────┘  │
│                          ↓                              │
│  ┌──────────────────────────────────────────────────┐  │
│  │  4. Distribution Engine (MODERATE only)          │  │
│  │     - Weighted distribution                      │  │
│  │     - Linearly decreasing weights                │  │
│  │     - Natural frequency pattern                  │  │
│  └──────────────────────────────────────────────────┘  │
│                          ↓                              │
│  ┌──────────────────────────────────────────────────┐  │
│  │  5. Output: List<String> queries                 │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 8.2 Query Templates (Hardcoded)

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

### 8.3 Parameter Ranges (Hardcoded)

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

### 8.4 Query Builder

```java
public class QueryBuilder {
    
    private Random random = new Random();
    
    /**
     * Generate a list of unique queries.
     */
    public List<String> generateUniqueQueries(int count) {
        List<String> queries = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            queries.add(generateRandomQuery());
        }
        
        return queries;
    }
    
    /**
     * Generate one random query from templates.
     */
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
    
    private String generateCourseQuery() {
        String template = randomFrom(QueryTemplates.COURSE_TEMPLATES);
        return fillParameters(template);
    }
    
    private String generateEnrollmentQuery() {
        String template = randomFrom(QueryTemplates.ENROLLMENT_TEMPLATES);
        return fillParameters(template);
    }
    
    private String generateDepartmentQuery() {
        String template = randomFrom(QueryTemplates.DEPARTMENT_TEMPLATES);
        return fillParameters(template);
    }
    
    private String generateJoinQuery() {
        String template = randomFrom(QueryTemplates.JOIN_TEMPLATES);
        return fillParameters(template);
    }
    
    /**
     * Replace ? placeholders with random values.
     */
    private String fillParameters(String template) {
        String result = template;
        
        while (result.contains("?")) {
            Object value = generateRandomParameter(template);
            result = result.replaceFirst("\\?", String.valueOf(value));
        }
        
        return result;
    }
    
    private Object generateRandomParameter(String template) {
        // Detect parameter type based on context
        if (template.contains("student_id")) {
            return randomInt(ParameterRanges.STUDENT_ID_MIN, ParameterRanges.STUDENT_ID_MAX);
        } else if (template.contains("department_id")) {
            return randomInt(ParameterRanges.DEPARTMENT_ID_MIN, ParameterRanges.DEPARTMENT_ID_MAX);
        } else if (template.contains("course_id")) {
            return randomInt(ParameterRanges.COURSE_ID_MIN, ParameterRanges.COURSE_ID_MAX);
        } else if (template.contains("credits")) {
            return randomInt(ParameterRanges.CREDITS_MIN, ParameterRanges.CREDITS_MAX);
        } else if (template.contains("year")) {
            return randomInt(ParameterRanges.YEAR_MIN, ParameterRanges.YEAR_MAX);
        } else if (template.contains("gpa")) {
            return randomDouble(ParameterRanges.GPA_MIN, ParameterRanges.GPA_MAX);
        } else if (template.contains("semester")) {
            return "'" + randomFrom(ParameterRanges.SEMESTERS) + "'";
        } else if (template.contains("grade")) {
            return "'" + randomFrom(ParameterRanges.GRADES) + "'";
        } else if (template.contains("name") && template.contains("departments")) {
            return "'" + randomFrom(ParameterRanges.DEPARTMENT_NAMES) + "'";
        } else {
            // Default: student ID
            return randomInt(ParameterRanges.STUDENT_ID_MIN, ParameterRanges.STUDENT_ID_MAX);
        }
    }
    
    private int randomInt(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
    
    private double randomDouble(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
    
    private <T> T randomFrom(T[] array) {
        return array[random.nextInt(array.length)];
    }
}
```

### 8.5 Distribution Engine (MODERATE Only)

```java
public class DistributionEngine {
    
    private Random random = new Random();
    
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
     * This creates a natural frequency pattern where some queries are more
     * popular than others, but without extreme skew.
     * 
     * @param uniqueQueries Pool of unique queries
     * @param totalRequests Total number of requests to generate
     * @return List of queries (with repetitions based on distribution)
     */
    public List<String> applyModerateDistribution(
            List<String> uniqueQueries,
            int totalRequests) {
        
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
}
```

**Example with 5 queries:**
```
Query 0: weight 5 (most frequent)
Query 1: weight 4
Query 2: weight 3
Query 3: weight 2
Query 4: weight 1 (least frequent)

Total weight = 15

Probability of selecting Query 0 = 5/15 = 33%
Probability of selecting Query 1 = 4/15 = 27%
Probability of selecting Query 2 = 3/15 = 20%
Probability of selecting Query 3 = 2/15 = 13%
Probability of selecting Query 4 = 1/15 = 7%
```

### 8.6 Complete Workload Generator

```java
public class WorkloadGenerator {
    
    private final QueryBuilder queryBuilder;
    private final DistributionEngine distributionEngine;
    
    public WorkloadGenerator() {
        this.queryBuilder = new QueryBuilder();
        this.distributionEngine = new DistributionEngine();
    }
    
    /**
     * Generate a complete workload.
     * 
     * @param totalRequests Total number of queries to generate
     * @param uniqueQueries Number of unique queries in the pool
     * @return List of queries ready for execution
     */
    public List<String> generateWorkload(int totalRequests, int uniqueQueries) {
        // Step 1: Generate unique queries
        List<String> uniqueQueryPool = queryBuilder.generateUniqueQueries(uniqueQueries);
        
        // Step 2: Apply MODERATE distribution to create workload
        List<String> workload = distributionEngine.applyModerateDistribution(
            uniqueQueryPool,
            totalRequests
        );
        
        return workload;
    }
}
```

### 8.7 Usage Example

```java
// Generate workload
WorkloadGenerator generator = new WorkloadGenerator();

List<String> workload = generator.generateWorkload(
    10000,  // 10,000 total requests
    1000    // 1,000 unique queries
);

// Execute workload
BenchmarkRunner runner = new BenchmarkRunner();
BenchmarkMetrics metrics = runner.runWorkload(workload, cacheEnabled);
```

### 8.8 Benchmark Config

```java
public class BenchmarkConfig {
    private int totalRequests;        // e.g., 10000
    private int uniqueQueries;        // e.g., 1000
    private int cacheCapacity;        // e.g., 100
    private long ttlSeconds;          // e.g., 60
    
    // Constructor, getters...
}
```

Note: Distribution is fixed to MODERATE (weighted distribution with linearly decreasing weights). No configuration needed.

### 8.9 Benchmark Metrics

```java
public class BenchmarkMetrics {
    private int totalRequests;
    private int dbQueries;
    private long cacheHits;
    private long cacheMisses;
    private double hitRate;
    private long totalTimeMs;
    private double avgLatencyMs;
    
    // Getters, calculation methods...
    
    public double calculateSpeedup(BenchmarkMetrics other) {
        return (double) other.totalTimeMs / this.totalTimeMs;
    }
    
    public double calculateDbLoadReduction(BenchmarkMetrics other) {
        return 1.0 - ((double) this.dbQueries / other.dbQueries);
    }
}
```

---

## 9. UI Layer Design

### 9.1 Main Frame

```java
public class MainFrame extends JFrame {
    
    private JTabbedPane tabbedPane;
    
    public MainFrame() {
        setTitle("Database Query Cache System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        tabbedPane = new JTabbedPane();
        
        // Add tabs
        tabbedPane.addTab("Manual Query", new ManualQueryPanel());
        tabbedPane.addTab("Benchmark", new BenchmarkPanel());
        tabbedPane.addTab("Cache Stats", new CacheStatsPanel());
        
        add(tabbedPane);
    }
}
```

### 9.2 Manual Query Dialog

**Components**:
- JTextArea: SQL query input
- JButton: Execute
- JTable: Results display
- JLabel: Cache status (HIT/MISS)
- JLabel: Response time
- JLabel: Database accessed (Yes/No)

**Layout**:
```
┌─────────────────────────────────────────┐
│  SQL Query:                             │
│  ┌───────────────────────────────────┐ │
│  │ SELECT * FROM students            │ │
│  │ WHERE id = 10                     │ │
│  └───────────────────────────────────┘ │
│                                         │
│  [Execute Query]                        │
│                                         │
│  Status: CACHE HIT  |  Time: 2ms       │
│  Database Accessed: No                  │
│                                         │
│  Results:                               │
│  ┌───────────────────────────────────┐ │
│  │ id │ name  │ email │ dept_id     │ │
│  ├────┼───────┼───────┼─────────────┤ │
│  │ 10 │ John  │ j@... │ 3           │ │
│  └───────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

### 9.3 Benchmark Dialog

**Components**:
- Configuration form:
  - JSpinner: Total requests
  - JSpinner: Unique queries
  - JSpinner: Cache capacity
  - JSpinner: TTL (seconds)
- JButton: Run Benchmark
- JTable: Results comparison

**Layout**:
```
┌─────────────────────────────────────────┐
│  Benchmark Configuration                │
│                                         │
│  Total Requests:    [10000 ▼]           │
│  Unique Queries:    [1000  ▼]           │
│  Cache Capacity:    [100   ▼]           │
│  TTL (seconds):     [60    ▼]           │
│                                         │
│  [Run Benchmark]                        │
│                                         │
│  Results:                               │
│  ┌───────────────────────────────────┐ │
│  │ Metric        │ No Cache │ Cache  │ │
│  ├───────────────┼──────────┼────────┤ │
│  │ Requests      │ 10000    │ 10000  │ │
│  │ DB Queries    │ 10000    │ 2150   │ │
│  │ Cache Hits    │ -        │ 7850   │ │
│  │ Hit Rate      │ -        │ 78.5%  │ │
│  │ Total Time    │ 8.4s     │ 2.2s   │ │
│  │ Avg Latency   │ 0.84ms   │ 0.22ms │ │
│  └───────────────────────────────────┘ │
│                                         │
│  Speedup: 3.82x                         │
│  DB Load Reduction: 78.5%               │
└─────────────────────────────────────────┘
```

### 9.4 Cache Stats Dialog

**Components**:
- JLabel: Cache size / capacity
- JLabel: Hits / Misses
- JLabel: Hit rate
- JLabel: Evictions
- JButton: Clear Cache
- JButton: Refresh

**Layout**:
```
┌─────────────────────────────────────────┐
│  Cache Statistics                       │
│                                         │
│  Size:        45 / 100                  │
│  Hits:        7850                      │
│  Misses:      2150                      │
│  Hit Rate:    78.5%                     │
│  Evictions:   12                        │
│                                         │
│  [Clear Cache]  [Refresh]               │
└─────────────────────────────────────────┘
```

---

## 10. Database Schema

### 10.1 Schema Definition

```sql
-- Departments table
CREATE TABLE departments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    head VARCHAR(100)
);

-- Students table (~1000 rows)
CREATE TABLE students (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    department_id INT,
    year INT,
    gpa DECIMAL(3,2),
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- Courses table (~100 rows)
CREATE TABLE courses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    credits INT,
    department_id INT,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- Enrollments table (~2000 rows)
CREATE TABLE enrollments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT,
    course_id INT,
    semester VARCHAR(20),
    grade VARCHAR(2),
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);
```

### 10.2 Sample Queries for Testing

```sql
-- Single row lookup
SELECT * FROM students WHERE id = 10;

-- Filter by department
SELECT * FROM students WHERE department_id = 3;

-- Course lookup
SELECT * FROM courses WHERE credits > 3;

-- Enrollment lookup
SELECT * FROM enrollments WHERE student_id = 5;

-- Aggregate query
SELECT COUNT(*) FROM students;

-- Join query
SELECT s.name, c.title 
FROM students s 
JOIN enrollments e ON s.id = e.student_id 
JOIN courses c ON e.course_id = c.id 
WHERE s.department_id = 2;
```

---

## 11. Configuration

### 11.1 Application Configuration

Create `config.properties`:

```properties
# Database connection
db.url=jdbc:mysql://localhost:3306/dbcache
db.username=root
db.password=yourpassword

# Cache settings
cache.capacity=100
cache.ttl.seconds=60
cache.policy=LRU
```

### 11.2 Configuration Loader

```java
public class Config {
    private static Properties props = new Properties();
    
    public static void load() throws IOException {
        try (InputStream input = new FileInputStream("config.properties")) {
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
```

---

## 12. Project Structure

```
database-cache/
├── pom.xml
├── config.properties
├── src/
│   ├── main/
│   │   ├── java/com/dbcache/
│   │   │   ├── Main.java
│   │   │   ├── config/
│   │   │   │   └── Config.java
│   │   │   ├── cache/
│   │   │   │   ├── CacheEngine.java
│   │   │   │   ├── LRUCache.java
│   │   │   │   ├── CacheEntry.java
│   │   │   │   ├── CacheConfig.java
│   │   │   │   ├── CacheStats.java
│   │   │   │   └── EvictionPolicy.java
│   │   │   ├── database/
│   │   │   │   ├── DatabaseManager.java
│   │   │   │   └── QueryResult.java
│   │   │   ├── handler/
│   │   │   │   └── RequestHandler.java
│   │   │   ├── benchmark/
│   │   │   │   ├── BenchmarkRunner.java
│   │   │   │   ├── WorkloadGenerator.java
│   │   │   │   ├── QueryBuilder.java
│   │   │   │   ├── QueryTemplates.java
│   │   │   │   ├── ParameterRanges.java
│   │   │   │   ├── DistributionEngine.java
│   │   │   │   ├── BenchmarkConfig.java
│   │   │   │   └── BenchmarkMetrics.java
│   │   │   ├── model/
│   │   │   │   ├── QueryResponse.java
│   │   │   │   └── QueryValidator.java
│   │   │   └── ui/
│   │   │       ├── MainFrame.java
│   │   │       ├── panel/
│   │   │       │   ├── ManualQueryPanel.java
│   │   │       │   ├── BenchmarkPanel.java
│   │   │       │   └── CacheStatsPanel.java
│   │   │       └── component/
│   │   │           └── ResultsTable.java
│   │   └── resources/
│   └── test/
│       └── java/com/dbcache/
│           ├── cache/
│           │   └── LRUCacheTest.java
│           ├── database/
│           │   └── DatabaseManagerTest.java
│           └── benchmark/
│               └── WorkloadGeneratorTest.java
├── sql/
│   ├── schema.sql
│   └── seed.sql
└── docs/
    └── ARCHITECTURE.md
```

---

## 13. Implementation Phases

### Phase 1: Foundation (Week 1-2)
**Goal**: Basic query execution without cache

**Tasks**:
- [ ] Setup Maven project structure
- [ ] Create database schema and seed data
- [ ] Implement DatabaseManager
- [ ] Implement QueryResult
- [ ] Implement basic RequestHandler (no cache yet)
- [ ] Create MainFrame with ManualQueryPanel
- [ ] Test: Execute queries, display results

**Deliverable**: Can execute SQL queries and see results in UI

### Phase 2: Cache Engine (Week 3-4)
**Goal**: Implement caching with LRU and TTL

**Tasks**:
- [ ] Implement CacheEngine interface
- [ ] Implement LRUCache
- [ ] Implement CacheEntry with TTL
- [ ] Update RequestHandler to use cache
- [ ] Add cache stats tracking
- [ ] Create CacheStatsPanel
- [ ] Test: Cache hits/misses, TTL expiration, LRU eviction

**Deliverable**: Queries are cached, cache stats displayed

### Phase 3: Benchmarking (Week 5-6)
**Goal**: Benchmark system with workload generation

**Tasks**:
- [ ] Implement QueryTemplates and ParameterRanges
- [ ] Implement QueryBuilder
- [ ] Implement DistributionEngine (MODERATE only)
- [ ] Implement WorkloadGenerator
- [ ] Implement BenchmarkRunner
- [ ] Implement BenchmarkConfig and BenchmarkMetrics
- [ ] Create BenchmarkPanel with configuration form
- [ ] Implement comparison table display
- [ ] Test: Run benchmarks with MODERATE distribution

**Deliverable**: Can run benchmarks, see comparison results

### Phase 4: Polish & Experimentation (Week 7-8)
**Goal**: Additional features and experimentation

**Tasks**:
- [ ] Add FIFO eviction policy
- [ ] Add query validation improvements
- [ ] Improve UI layout and usability
- [ ] Add experimental features (capacity vs hit rate)
- [ ] Write documentation
- [ ] Prepare presentation

**Deliverable**: Complete system ready for demonstration

---

## 14. Testing Strategy

### 14.1 Unit Tests

**Cache Engine Tests**:
- Test cache hit/miss
- Test LRU eviction
- Test TTL expiration
- Test cache key normalization
- Test capacity limits

**Database Manager Tests**:
- Test query execution
- Test ResultSet conversion
- Test error handling

**Workload Generator Tests**:
- Test query generation from templates
- Test MODERATE distribution (verify weighted selection)
- Test parameter range boundaries

### 14.2 Integration Tests

- Test full query flow (UI → Handler → Cache → DB)
- Test benchmark execution
- Test cache configuration changes

### 14.3 Manual Testing

- Execute various queries manually
- Verify cache behavior
- Run benchmarks with different configurations
- Compare results with expectations

---

## 15. Future Enhancements (V2)

### 15.1 Concurrency
- Thread-safe cache implementation
- Concurrent benchmark execution
- Progress indicators for long operations

### 15.2 Visualization
- Charts for benchmark results (JFreeChart)
- Hit rate over time
- Cache size over time

### 15.3 Advanced Cache Features
- LFU (Least Frequently Used) eviction
- Cache warming
- Query result size limits

### 15.4 Query Validation
- Detect non-deterministic queries (NOW(), RAND())
- Semantic SQL equivalence detection

### 15.5 Persistence
- Save/load cache to disk
- Persistent cache across restarts

---

## 16. Key Design Decisions

### 16.1 Why LRU over LFU?
**Decision**: Use LRU for V1

**Rationale**:
- Simpler to implement (LinkedHashMap with access-order)
- Good performance for most workloads
- Well-understood algorithm
- LFU requires frequency tracking, more complex

### 16.2 Why Lazy TTL Expiration?
**Decision**: Check expiration on access, not background thread

**Rationale**:
- Simpler implementation
- No threading required for V1
- Acceptable for sequential execution model
- Background cleanup adds complexity without clear benefit in V1

### 16.3 Why Sequential Execution?
**Decision**: Single-threaded, sequential query execution

**Rationale**:
- Simpler to implement and debug
- Clearer performance measurements
- No concurrency overhead muddying results
- Can add concurrency in V2 if needed

### 16.4 Why Swing over JavaFX or Web?
**Decision**: Use Swing for UI

**Rationale**:
- Built into Java (no additional dependencies)
- Simpler for desktop application
- Team familiarity
- Sufficient for project requirements

### 16.5 Why MODERATE-Only Distribution?
**Decision**: Use only MODERATE distribution (weighted, linearly decreasing weights)

**Rationale**:
- Simpler implementation — no need for multiple distribution algorithms
- MODERATE provides a realistic middle ground between uniform and extreme skew
- Linearly decreasing weights are easy to understand and explain
- Sufficient for demonstrating cache effectiveness
- Reduces complexity for a student project

---

## 17. API Reference (Quick Reference)

### 17.1 RequestHandler API

```java
// Initialize
RequestHandler handler = new RequestHandler(dbManager, cacheConfig);

// Execute query
QueryResponse response = handler.executeQuery("SELECT * FROM students WHERE id = 10");

// Check response
if (response.isSuccessful()) {
    QueryResult result = response.getResult();
    boolean cacheHit = response.isCacheHit();
    long timeMs = response.getResponseTimeMs();
    boolean dbAccessed = response.isDatabaseAccessed();
}

// Get stats
CacheStats stats = handler.getCacheStats();

// Clear cache
handler.clearCache();

// Configure cache
handler.configureCache(new CacheConfig(200, 120, EvictionPolicy.LRU));
```

### 17.2 QueryResponse API

```java
QueryResponse response = ...;

// Check success
boolean success = response.isSuccessful();

// Get result
QueryResult result = response.getResult();

// Get metadata
boolean cacheHit = response.isCacheHit();
long responseTimeMs = response.getResponseTimeMs();
boolean dbAccessed = response.isDatabaseAccessed();

// Get error
String error = response.getErrorMessage();
```

### 17.3 QueryResult API

```java
QueryResult result = ...;

// Get columns
List<String> columns = result.getColumns();

// Get rows
List<List<Object>> rows = result.getRows();

// Get counts
int rowCount = result.getRowCount();
int columnCount = result.getColumnCount();
```

### 17.4 CacheStats API

```java
CacheStats stats = ...;

// Get metrics
int size = stats.getSize();
int capacity = stats.getCapacity();
long hits = stats.getHits();
long misses = stats.getMisses();
long evictions = stats.getEvictions();
double hitRate = stats.getHitRate();
```

---

## 18. Glossary

- **Cache Hit**: Query result found in cache, database not accessed
- **Cache Miss**: Query result not in cache, must query database
- **TTL (Time To Live)**: How long a cache entry remains valid
- **LRU (Least Recently Used)**: Eviction policy that removes least recently accessed entries
- **FIFO (First In, First Out)**: Eviction policy that removes oldest entries
- **Eviction**: Removing entries from cache when capacity is reached
- **Workload Distribution**: Pattern of query frequency (MODERATE: weighted distribution with linearly decreasing weights)
- **Speedup**: Ratio of execution time without cache vs with cache
- **DB Load Reduction**: Percentage reduction in database queries due to caching

---

## 19. Contact & Support

For questions about this architecture:
- Review the component diagrams and data flows
- Check the interface contracts in Section 3
- Refer to the implementation examples throughout

Good luck with the implementation!
