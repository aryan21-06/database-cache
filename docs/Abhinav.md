# Cache Engine Developer Guide - Database Query Cache System

## What You'll Build

You are responsible for implementing the **Cache Engine** — the core component that stores query results in memory to avoid repeated database queries.

Your cache will:
1. Store query results (QueryResult objects)
2. Retrieve cached results quickly (cache hit)
3. Handle cache misses (when result is not cached)
4. Evict old entries when capacity is reached (LRU policy)
5. Expire entries after TTL (Time To Live)
6. Track statistics (hits, misses, evictions)

---

## Understanding the Cache

### What is a Cache?

Think of a cache like a **quick-reference notebook**:

**Without cache:**
```
User asks: "SELECT * FROM students WHERE id = 10"
    ↓
Application queries database (slow: ~50ms)
    ↓
Returns result
```

**With cache:**
```
User asks: "SELECT * FROM students WHERE id = 10"
    ↓
Check cache: Is this query already cached?
    ↓
┌─ YES (Cache Hit) ─┐  ┌─ NO (Cache Miss) ─┐
│ Return cached     │  │ Query database     │
│ result (~1ms)     │  │ (~50ms)            │
│                   │  │ Store in cache     │
│                   │  │ Return result      │
└───────────────────┘  └───────────────────┘
```

### Why LRU?

**LRU = Least Recently Used**

When cache is full, remove the entry that hasn't been accessed for the longest time.

**Example:**
```
Cache capacity: 3

1. Cache: [A, B, C]
2. Access A → Cache: [B, C, A] (A moved to end, most recent)
3. Add D → Cache: [C, A, D] (B evicted, least recent)
```

---

## Your Responsibilities

You need to implement these files:

### 1. CacheEngine.java (Interface)
**File**: `src/main/java/com/dbcache/cache/CacheEngine.java`

This is already defined. You must implement all methods.

### 2. LRUCache.java (Main Implementation)
**File**: `src/main/java/com/dbcache/cache/LRUCache.java`

This is where you'll write most of your code.

### 3. CacheEntry.java (Helper Class)
**File**: `src/main/java/com/dbcache/cache/CacheEntry.java`

Already mostly implemented. Stores metadata for each cached query.

### 4. CacheStats.java (Data Class)
**File**: `src/main/java/com/dbcache/cache/CacheStats.java`

Already implemented. Just a data container.

### 5. CacheConfig.java (Configuration)
**File**: `src/main/java/com/dbcache/cache/CacheConfig.java`

Already implemented. Holds capacity, TTL, eviction policy.

### 6. EvictionPolicy.java (Enum)
**File**: `src/main/java/com/dbcache/cache/EvictionPolicy.java`

Already implemented. Just LRU and FIFO for now.

---

## Exact Contracts (Input/Output)

### CacheEngine Interface

```java
public interface CacheEngine {
    
    /**
     * Retrieve a cached query result.
     * 
     * @param sql The SQL query (will be normalized internally)
     * @return The cached QueryResult, or null if not found or expired
     */
    QueryResult get(String sql);
    
    /**
     * Store a query result in the cache.
     * 
     * @param sql The SQL query (will be normalized internally)
     * @param result The query result to cache
     */
    void put(String sql, QueryResult result);
    
    /**
     * Clear all entries from the cache and reset statistics.
     */
    void clear();
    
    /**
     * Get current cache statistics.
     * 
     * @return CacheStats object with hits, misses, evictions, etc.
     */
    CacheStats getStats();
    
    /**
     * Reconfigure the cache (change capacity, TTL, etc.).
     * 
     * @param config New cache configuration
     */
    void configure(CacheConfig config);
}
```

### Method Specifications

#### 1. `QueryResult get(String sql)`

**Input:**
- `sql`: A SQL query string (e.g., "SELECT * FROM students WHERE id = 10")
- Can be null or empty

**Output:**
- Returns `QueryResult` if query is cached and not expired
- Returns `null` if:
  - Query is not in cache (cache miss)
  - Query is in cache but expired (TTL exceeded)
  - Input is null or empty

**Side Effects:**
- Increments `hits` counter if cache hit
- Increments `misses` counter if cache miss
- Updates `lastAccessedAt` timestamp on cache hit
- Removes expired entries from cache

**Behavior:**
```java
// Scenario 1: Cache miss
QueryResult result = cache.get("SELECT * FROM students WHERE id = 1");
// Returns: null
// Stats: misses = 1

// Scenario 2: Cache hit
cache.put("SELECT * FROM students WHERE id = 1", someResult);
QueryResult result = cache.get("SELECT * FROM students WHERE id = 1");
// Returns: someResult
// Stats: hits = 1

// Scenario 3: Expired entry
cache.put("SELECT * FROM students WHERE id = 1", someResult);
Thread.sleep(61000); // Wait 61 seconds (TTL = 60s)
QueryResult result = cache.get("SELECT * FROM students WHERE id = 1");
// Returns: null (expired)
// Stats: misses = 1, entry removed from cache

// Scenario 4: Normalization
cache.put("SELECT * FROM students WHERE id = 1", someResult);
QueryResult result = cache.get("select * from students where id = 1");
// Returns: someResult (normalized to same key)
```

#### 2. `void put(String sql, QueryResult result)`

**Input:**
- `sql`: A SQL query string
- `result`: A QueryResult object to cache
- Both can be null (should handle gracefully)

**Output:**
- None (void)

**Side Effects:**
- Stores the query result in cache
- Removes oldest entry if cache is at capacity (LRU eviction)
- Increments `evictions` counter if an entry was evicted

**Behavior:**
```java
// Scenario 1: Normal insert
cache.put("SELECT * FROM students WHERE id = 1", result1);
// Cache now contains this query

// Scenario 2: Update existing entry
cache.put("SELECT * FROM students WHERE id = 1", result1);
cache.put("SELECT * FROM students WHERE id = 1", result2);
// Cache now contains result2 (replaced)

// Scenario 3: Capacity eviction
// Assume capacity = 3
cache.put("query1", result1);
cache.put("query2", result2);
cache.put("query3", result3);
cache.put("query4", result4);
// Cache now contains: query2, query3, query4 (query1 evicted)
// Stats: evictions = 1

// Scenario 4: Null handling
cache.put(null, result);
cache.put("query", null);
// Should not crash, handle gracefully (ignore or throw exception)
```

#### 3. `void clear()`

**Input:**
- None

**Output:**
- None

**Side Effects:**
- Removes all entries from cache
- Resets all statistics to 0 (hits, misses, evictions)

**Behavior:**
```java
cache.put("query1", result1);
cache.put("query2", result2);
cache.get("query1"); // hit
cache.get("query3"); // miss

cache.clear();
// Cache is now empty
// Stats: hits = 0, misses = 0, evictions = 0, size = 0
```

#### 4. `CacheStats getStats()`

**Input:**
- None

**Output:**
- Returns a `CacheStats` object with current statistics

**Behavior:**
```java
cache.put("query1", result1);
cache.get("query1"); // hit
cache.get("query2"); // miss

CacheStats stats = cache.getStats();
// stats.getSize() = 1
// stats.getCapacity() = 100 (or whatever was configured)
// stats.getHits() = 1
// stats.getMisses() = 1
// stats.getEvictions() = 0
// stats.getHitRate() = 0.5 (1 hit / 2 total requests)
```

#### 5. `void configure(CacheConfig config)`

**Input:**
- `config`: A CacheConfig object with new settings

**Output:**
- None

**Side Effects:**
- Updates cache capacity (may evict entries if new capacity is smaller)
- Updates TTL for future entries (existing entries keep their original TTL)

**Behavior:**
```java
CacheConfig config = new CacheConfig(50, 120, EvictionPolicy.LRU);
cache.configure(config);
// Cache capacity is now 50
// New entries will have TTL of 120 seconds
```

---

## Algorithm Details

### LRU Implementation Using LinkedHashMap

**Key Insight:** Java's `LinkedHashMap` has a built-in LRU mode!

```java
// Create LinkedHashMap with access-order = true
Map<String, CacheEntry> cache = new LinkedHashMap<>(
    initialCapacity,
    loadFactor,
    true  // ← This enables access-order (LRU behavior)
) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
        // Return true to remove the eldest entry
        boolean shouldRemove = size() > capacity;
        if (shouldRemove) {
            evictions++;
        }
        return shouldRemove;
    }
};
```

**How it works:**
- When you `get(key)`, the entry moves to the end (most recently used)
- When you `put(key, value)`, if size > capacity, the eldest entry (first in list) is removed
- The `removeEldestEntry` method is called automatically after each `put`

### Cache Key Normalization

**Problem:** These queries should map to the same cache entry:
```sql
SELECT * FROM students WHERE id = 10
select * from students where id = 10
SELECT   *   FROM   students   WHERE   id=10;
```

**Solution:** Normalize the query before using it as a key.

```java
private String normalizeQuery(String sql) {
    if (sql == null) return null;
    
    return sql.toLowerCase()                    // 1. Lowercase
              .trim()                           // 2. Trim whitespace
              .replaceAll(";\\s*$", "")         // 3. Remove trailing semicolons
              .replaceAll("\\s+", " ");         // 4. Collapse multiple spaces
}
```

**Examples:**
```
Input:  "SELECT * FROM students WHERE id = 10;"
Output: "select * from students where id = 10"

Input:  "  SELECT   *   FROM   students   WHERE   id=10  "
Output: "select * from students where id=10"

Input:  "select * from students where id = 10"
Output: "select * from students where id = 10"
```

### TTL (Time To Live) Strategy

**Approach:** Lazy expiration (check on access)

**Why lazy?**
- Simpler implementation
- No background thread needed
- Acceptable for V1 (single-threaded)

**Implementation:**
```java
public QueryResult get(String sql) {
    String key = normalizeQuery(sql);
    CacheEntry entry = cache.get(key);
    
    if (entry == null) {
        misses++;
        return null;
    }
    
    // Check if expired
    if (isExpired(entry)) {
        cache.remove(key);  // Remove expired entry
        misses++;
        return null;
    }
    
    // Cache hit
    entry.recordAccess();
    hits++;
    return entry.getResult();
}

private boolean isExpired(CacheEntry entry) {
    long age = System.currentTimeMillis() - entry.getCreatedAt();
    return age > ttlMillis;
}
```

**Behavior:**
- Expired entries are removed when accessed (not before)
- Expired entries count as cache misses
- No background cleanup thread

### Statistics Tracking

**What to track:**
- `hits`: Number of successful cache lookups
- `misses`: Number of failed cache lookups (not found or expired)
- `evictions`: Number of entries removed due to capacity limits
- `size`: Current number of entries in cache
- `hitRate`: hits / (hits + misses), or 0.0 if no requests yet

**When to increment:**
```java
// In get() method:
if (entry == null || isExpired(entry)) {
    misses++;
} else {
    hits++;
}

// In put() method (inside removeEldestEntry):
if (size() > capacity) {
    evictions++;
}
```

**Hit rate calculation:**
```java
public double getHitRate() {
    long total = hits + misses;
    if (total == 0) return 0.0;
    return (double) hits / total;
}
```

---

## Step-by-Step Implementation Guide

### Step 1: Understand the Data Structures

**CacheEntry** (already implemented):
```java
public class CacheEntry {
    private final QueryResult result;      // The cached data
    private final long createdAt;          // When entry was created
    private long lastAccessedAt;           // Last access time
    private int accessCount;               // Number of accesses
    
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

**CacheStats** (already implemented):
```java
public class CacheStats {
    private int size;
    private int capacity;
    private long hits;
    private long misses;
    private long evictions;
    private double hitRate;
    
    // Constructor, getters...
}
```

### Step 2: Implement LRUCache

Open `src/main/java/com/dbcache/cache/LRUCache.java`

**Constructor:**
```java
public LRUCache(int capacity, long ttlSeconds) {
    this.capacity = capacity;
    this.ttlMillis = ttlSeconds * 1000;
    
    // Create LinkedHashMap with access-order = true
    this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
            boolean shouldRemove = size() > LRUCache.this.capacity;
            if (shouldRemove) {
                evictions++;
            }
            return shouldRemove;
        }
    };
}
```

**get() method:**
```java
@Override
public QueryResult get(String sql) {
    if (sql == null || sql.trim().isEmpty()) {
        return null;
    }
    
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
    
    // Cache hit
    entry.recordAccess();
    hits++;
    return entry.getResult();
}
```

**put() method:**
```java
@Override
public void put(String sql, QueryResult result) {
    if (sql == null || result == null) {
        return;  // Ignore null inputs
    }
    
    String key = normalizeQuery(sql);
    CacheEntry entry = new CacheEntry(result);
    cache.put(key, entry);
}
```

**clear() method:**
```java
@Override
public void clear() {
    cache.clear();
    hits = 0;
    misses = 0;
    evictions = 0;
}
```

**getStats() method:**
```java
@Override
public CacheStats getStats() {
    long total = hits + misses;
    double hitRate = (total == 0) ? 0.0 : (double) hits / total;
    
    return new CacheStats(
        cache.size(),
        capacity,
        hits,
        misses,
        evictions,
        hitRate
    );
}
```

**configure() method:**
```java
@Override
public void configure(CacheConfig config) {
    this.capacity = config.getCapacity();
    this.ttlMillis = config.getTtlSeconds() * 1000;
    
    // If new capacity is smaller, evict entries
    while (cache.size() > capacity) {
        // Remove eldest entry
        Iterator<String> it = cache.keySet().iterator();
        if (it.hasNext()) {
            it.next();
            it.remove();
            evictions++;
        }
    }
}
```

**Helper methods:**
```java
private boolean isExpired(CacheEntry entry) {
    long age = System.currentTimeMillis() - entry.getCreatedAt();
    return age > ttlMillis;
}

private String normalizeQuery(String sql) {
    if (sql == null) return null;
    
    return sql.toLowerCase()
              .trim()
              .replaceAll(";\\s*$", "")
              .replaceAll("\\s+", " ");
}
```

### Step 3: Test Your Implementation

Create a test file: `src/test/java/com/dbcache/cache/LRUCacheTest.java`

**Test 1: Basic put and get**
```java
@Test
void testPutAndGet() {
    LRUCache cache = new LRUCache(10, 60);
    
    QueryResult result = new QueryResult(
        Arrays.asList("id", "name"),
        Arrays.asList(Arrays.asList(1, "John"))
    );
    
    cache.put("SELECT * FROM students WHERE id = 1", result);
    QueryResult retrieved = cache.get("SELECT * FROM students WHERE id = 1");
    
    assertNotNull(retrieved);
    assertEquals(1, retrieved.getRows().get(0).get(0));
}
```

**Test 2: Cache miss**
```java
@Test
void testCacheMiss() {
    LRUCache cache = new LRUCache(10, 60);
    
    QueryResult result = cache.get("SELECT * FROM students WHERE id = 1");
    
    assertNull(result);
    assertEquals(1, cache.getStats().getMisses());
}
```

**Test 3: Cache hit**
```java
@Test
void testCacheHit() {
    LRUCache cache = new LRUCache(10, 60);
    
    QueryResult result = new QueryResult(
        Arrays.asList("id", "name"),
        Arrays.asList(Arrays.asList(1, "John"))
    );
    
    cache.put("SELECT * FROM students WHERE id = 1", result);
    cache.get("SELECT * FROM students WHERE id = 1");
    
    assertEquals(1, cache.getStats().getHits());
    assertEquals(0, cache.getStats().getMisses());
}
```

**Test 4: LRU eviction**
```java
@Test
void testLRUEviction() {
    LRUCache cache = new LRUCache(3, 60);
    
    cache.put("query1", result1);
    cache.put("query2", result2);
    cache.put("query3", result3);
    
    // Cache is full: [query1, query2, query3]
    
    cache.put("query4", result4);
    
    // query1 should be evicted (least recently used)
    assertNull(cache.get("query1"));
    assertNotNull(cache.get("query2"));
    assertNotNull(cache.get("query3"));
    assertNotNull(cache.get("query4"));
    
    assertEquals(1, cache.getStats().getEvictions());
}
```

**Test 5: TTL expiration**
```java
@Test
void testTTLExpiration() throws InterruptedException {
    LRUCache cache = new LRUCache(10, 1); // 1 second TTL
    
    cache.put("query1", result1);
    
    // Immediately should be cached
    assertNotNull(cache.get("query1"));
    
    // Wait for expiration
    Thread.sleep(1100);
    
    // Should be expired
    assertNull(cache.get("query1"));
}
```

**Test 6: Query normalization**
```java
@Test
void testQueryNormalization() {
    LRUCache cache = new LRUCache(10, 60);
    
    cache.put("SELECT * FROM students WHERE id = 1", result1);
    
    // Different case and whitespace should map to same key
    assertNotNull(cache.get("select * from students where id = 1"));
    assertNotNull(cache.get("SELECT   *   FROM   students   WHERE   id=1"));
    assertNotNull(cache.get("SELECT * FROM students WHERE id = 1;"));
}
```

**Test 7: Statistics**
```java
@Test
void testStatistics() {
    LRUCache cache = new LRUCache(10, 60);
    
    cache.put("query1", result1);
    cache.put("query2", result2);
    
    cache.get("query1"); // hit
    cache.get("query2"); // hit
    cache.get("query3"); // miss
    
    CacheStats stats = cache.getStats();
    assertEquals(2, stats.getSize());
    assertEquals(2, stats.getHits());
    assertEquals(1, stats.getMisses());
    assertEquals(2.0/3.0, stats.getHitRate(), 0.001);
}
```

**Test 8: Clear cache**
```java
@Test
void testClear() {
    LRUCache cache = new LRUCache(10, 60);
    
    cache.put("query1", result1);
    cache.get("query1"); // hit
    cache.get("query2"); // miss
    
    cache.clear();
    
    CacheStats stats = cache.getStats();
    assertEquals(0, stats.getSize());
    assertEquals(0, stats.getHits());
    assertEquals(0, stats.getMisses());
}
```

---

## Edge Cases to Handle

### 1. Null Inputs
```java
cache.get(null);          // Should return null, not crash
cache.put(null, result);  // Should ignore, not crash
cache.put("query", null); // Should ignore, not crash
```

### 2. Empty Strings
```java
cache.get("");            // Should return null
cache.put("", result);    // Should ignore
```

### 3. Capacity = 0
```java
LRUCache cache = new LRUCache(0, 60);
cache.put("query", result);
// Should handle gracefully (either reject or immediately evict)
```

### 4. TTL = 0
```java
LRUCache cache = new LRUCache(10, 0);
cache.put("query", result);
// Should expire immediately or handle gracefully
```

### 5. Very Large Capacity
```java
LRUCache cache = new LRUCache(1000000, 60);
// Should work fine, just uses more memory
```

### 6. Concurrent Access (Not Required for V1)
```java
// V1 is single-threaded, so no need to worry about this
// But if you want to be safe, you can add synchronized blocks
```

---

## Integration with Other Components

### How RequestHandler Uses Your Cache

```java
public class RequestHandler {
    private final CacheEngine cache;
    private final DatabaseManager dbManager;
    
    public QueryResponse executeQuery(String sql) {
        // 1. Try cache first
        QueryResult result = cache.get(sql);
        
        if (result != null) {
            // Cache hit!
            return QueryResponse.success(result, true, 1, false);
        }
        
        // 2. Cache miss - query database
        result = dbManager.executeQuery(sql);
        
        // 3. Store in cache
        cache.put(sql, result);
        
        // 4. Return response
        return QueryResponse.success(result, false, 50, true);
    }
}
```

### What RequestHandler Expects

1. `cache.get(sql)` returns `QueryResult` or `null`
2. `cache.put(sql, result)` stores the result
3. Cache handles normalization internally
4. Cache tracks statistics automatically
5. Cache handles TTL and eviction automatically

---

## Performance Considerations

### Time Complexity

| Operation | Expected Time |
|-----------|---------------|
| `get()`   | O(1)          |
| `put()`   | O(1)          |
| `clear()` | O(n)          |
| `getStats()` | O(1)       |

LinkedHashMap provides O(1) operations for get, put, and remove.

### Space Complexity

- Each cache entry stores:
  - QueryResult (columns + rows)
  - Metadata (timestamps, access count)
- Total space: O(n * avg_result_size)

### Memory Management

- Cache has a fixed capacity (configurable)
- When full, oldest entries are evicted
- Expired entries are removed on access (lazy)

---

## Testing Checklist

### Unit Tests
- [ ] Basic put and get
- [ ] Cache miss (query not in cache)
- [ ] Cache hit (query in cache)
- [ ] LRU eviction (capacity limit)
- [ ] TTL expiration
- [ ] Query normalization (case, whitespace, semicolons)
- [ ] Statistics tracking (hits, misses, evictions, hit rate)
- [ ] Clear cache
- [ ] Reconfigure cache
- [ ] Null input handling
- [ ] Empty string handling

### Integration Tests
- [ ] Work with RequestHandler
- [ ] Multiple sequential queries
- [ ] Cache hit after put
- [ ] Cache miss before put
- [ ] Statistics across multiple operations

### Performance Tests
- [ ] 10,000 put operations
- [ ] 10,000 get operations
- [ ] Mixed workload (50% hits, 50% misses)
- [ ] Capacity eviction under load

---

## Common Mistakes to Avoid

### 1. Forgetting to Normalize Queries
```java
// WRONG: Using raw SQL as key
cache.put("SELECT * FROM students", result);
cache.get("select * from students"); // Cache miss!

// RIGHT: Normalize before using as key
String key = normalizeQuery(sql);
cache.put(key, result);
```

### 2. Not Checking TTL on Get
```java
// WRONG: Returning expired entries
public QueryResult get(String sql) {
    return cache.get(normalizeQuery(sql));
}

// RIGHT: Check expiration
public QueryResult get(String sql) {
    CacheEntry entry = cache.get(normalizeQuery(sql));
    if (entry != null && isExpired(entry)) {
        cache.remove(normalizeQuery(sql));
        return null;
    }
    return entry != null ? entry.getResult() : null;
}
```

### 3. Not Updating Statistics
```java
// WRONG: Not tracking hits/misses
public QueryResult get(String sql) {
    return cache.get(normalizeQuery(sql));
}

// RIGHT: Track statistics
public QueryResult get(String sql) {
    CacheEntry entry = cache.get(normalizeQuery(sql));
    if (entry == null) {
        misses++;
        return null;
    }
    hits++;
    return entry.getResult();
}
```

### 4. Not Handling Null Inputs
```java
// WRONG: NPE on null
public QueryResult get(String sql) {
    String key = sql.toLowerCase(); // NullPointerException!
    return cache.get(key);
}

// RIGHT: Check for null
public QueryResult get(String sql) {
    if (sql == null) return null;
    String key = normalizeQuery(sql);
    return cache.get(key);
}
```

---

## Quick Reference

### Method Signatures
```java
QueryResult get(String sql);
void put(String sql, QueryResult result);
void clear();
CacheStats getStats();
void configure(CacheConfig config);
```

### Key Algorithms
- **LRU**: LinkedHashMap with access-order = true
- **Normalization**: lowercase + trim + remove semicolons + collapse spaces
- **TTL**: Check on access (lazy expiration)
- **Statistics**: Increment on each operation

### Data Structures
- **Cache**: `LinkedHashMap<String, CacheEntry>`
- **CacheEntry**: Stores QueryResult + metadata
- **CacheStats**: Stores hits, misses, evictions, hitRate

### Constants
- **Default capacity**: 100 entries
- **Default TTL**: 60 seconds
- **Default eviction policy**: LRU

---

## Summary

**What you need to implement:**
1. ✅ LRUCache class with all CacheEngine methods
2. ✅ LRU eviction using LinkedHashMap
3. ✅ TTL expiration (lazy, on access)
4. ✅ Query normalization
5. ✅ Statistics tracking
6. ✅ Null/empty input handling
7. ✅ Unit tests

**What's already implemented:**
- CacheEntry (metadata storage)
- CacheStats (statistics container)
- CacheConfig (configuration)
- EvictionPolicy (enum)

**What you need to deliver:**
- Working LRUCache implementation
- All unit tests passing
- Integration with RequestHandler

Good luck! You've got this.
