# Frontend Developer Guide - Database Query Cache System

## Overview

You are building the **Swing UI layer** for a database query caching system. The backend is already implemented (or being implemented in parallel). Your job is to create the user interface that allows users to:

1. Execute SQL queries manually and see results
2. Run benchmarks to measure cache performance
3. View cache statistics

**Key Point**: You will call Java methods directly. There is NO REST API, NO HTTP, NO JSON. Everything is direct method calls within the same JVM.

---

## Project Structure

The project already has a skeleton structure. You need to **fill in the TODOs** in these files:

### Your Files (UI Layer)

```
src/main/java/com/dbcache/
├── Main.java                              ← Entry point (mostly done, just connect UI)
├── ui/
│   ├── MainFrame.java                     ← Main window with tabs
│   ├── panel/
│   │   ├── ManualQueryPanel.java          ← Tab 1: Manual query execution
│   │   ├── BenchmarkPanel.java            ← Tab 2: Benchmark configuration & results
│   │   └── CacheStatsPanel.java           ← Tab 3: Cache statistics display
│   └── component/
│       └── ResultsTable.java              ← Reusable table component
```

### Backend Files (Already Exist - DO NOT MODIFY)

These files are being implemented by other team members. You will **call methods** from these classes:

```
src/main/java/com/dbcache/
├── config/
│   └── Config.java                        ← Loads config.properties
├── cache/
│   ├── CacheEngine.java                   ← Cache interface
│   ├── LRUCache.java                      ← LRU cache implementation
│   ├── CacheEntry.java                    ← Cache entry with metadata
│   ├── CacheConfig.java                   ← Cache configuration
│   ├── CacheStats.java                    ← Cache statistics (you'll use this)
│   └── EvictionPolicy.java                ← LRU/FIFO enum
├── database/
│   ├── DatabaseManager.java               ← JDBC operations
│   └── QueryResult.java                   ← Query result (you'll use this)
├── handler/
│   └── RequestHandler.java                ← Main entry point (you'll call this)
├── benchmark/
│   ├── BenchmarkRunner.java               ← Benchmark execution (you'll call this)
│   ├── BenchmarkConfig.java               ← Benchmark configuration (you'll create this)
│   ├── BenchmarkResults.java              ← Benchmark results (you'll use this)
│   └── BenchmarkMetrics.java              ← Metrics data (you'll use this)
└── model/
    ├── QueryResponse.java                 ← Query response wrapper (you'll use this)
    └── QueryValidator.java                ← Query validation
```

---

## What You Need to Do

### Step 1: Fill in the UI Skeleton Files

Each UI file already has a skeleton structure with TODO comments. You need to:

1. **Open the file** (e.g., `ManualQueryPanel.java`)
2. **Find the TODO comments**
3. **Implement the UI components** (buttons, labels, tables, etc.)
4. **Add action listeners** that call backend methods
5. **Update the UI** with results from backend

### Step 2: Import the Classes You Need

Add these imports to your UI files:

```java
// For ManualQueryPanel and CacheStatsPanel
import com.dbcache.handler.RequestHandler;
import com.dbcache.model.QueryResponse;
import com.dbcache.database.QueryResult;
import com.dbcache.cache.CacheStats;

// For BenchmarkPanel
import com.dbcache.benchmark.BenchmarkRunner;
import com.dbcache.benchmark.BenchmarkConfig;
import com.dbcache.benchmark.BenchmarkResults;
import com.dbcache.benchmark.BenchmarkMetrics;

// For MainFrame
import com.dbcache.handler.RequestHandler;
import com.dbcache.benchmark.BenchmarkRunner;
```

---

## Tab 1: Manual Query Panel

**File**: `src/main/java/com/dbcache/ui/panel/ManualQueryPanel.java`

### Components

```
┌─────────────────────────────────────────────────────────┐
│  SQL Query:                                             │
│  ┌───────────────────────────────────────────────────┐ │
│  │                                                   │ │
│  │  [JTextArea - 4 rows, monospace font]            │ │
│  │                                                   │ │
│  └───────────────────────────────────────────────────┘ │
│                                                         │
│  [Execute Query]  [Clear]                              │
│                                                         │
│  ┌───────────────────────────────────────────────────┐ │
│  │ Status: CACHE HIT  |  Time: 2ms  |  DB: No       │ │
│  └───────────────────────────────────────────────────┘ │
│                                                         │
│  Results:                                               │
│  ┌───────────────────────────────────────────────────┐ │
│  │ id │ name     │ email          │ department_id    │ │
│  ├────┼──────────┼────────────────┼──────────────────┤ │
│  │ 10 │ John Doe │ john@example.. │ 3                │ │
│  │ 11 │ Jane Doe │ jane@example.. │ 2                │
│  └───────────────────────────────────────────────────┘ │
│  [JTable with scroll pane]                             │
└─────────────────────────────────────────────────────────┘
```

### Component Details

#### 1. SQL Input Area
- **Type**: `JTextArea`
- **Rows**: 4
- **Font**: Monospace (e.g., "Monospaced", Font.PLAIN, 14)
- **Default text**: Empty
- **Placeholder**: "Enter SQL SELECT query here..."

#### 2. Buttons
- **Execute Query**: `JButton`
  - Action: Call `requestHandler.executeQuery(sql)`
  - Disabled state: While query is executing
- **Clear**: `JButton`
  - Action: Clear the text area and results table

#### 3. Status Panel
- **Type**: `JPanel` with `FlowLayout`
- **Components**: 3 `JLabel` instances
  - Status Label: "Status: CACHE HIT" or "Status: CACHE MISS" or "Status: ERROR"
  - Time Label: "Time: 2ms"
  - Database Label: "DB: Yes" or "DB: No"
- **Colors**:
  - CACHE HIT: Green text `new Color(0, 128, 0)`
  - CACHE MISS: Orange text `new Color(255, 140, 0)`
  - ERROR: Red text `Color.RED`

#### 4. Results Table
- **Type**: `JTable` inside `JScrollPane`
- **Columns**: Dynamic (based on query result)
- **Column headers**: From `QueryResult.getColumns()`
- **Data**: From `QueryResult.getRows()`
- **Row height**: 25 pixels
- **Font**: Monospace, 12pt

### Data Flow

```
User clicks "Execute Query"
    ↓
Get SQL from JTextArea
    ↓
Call: QueryResponse response = requestHandler.executeQuery(sql);
    ↓
Update Status Panel:
    - response.isCacheHit() → "CACHE HIT" (green) or "CACHE MISS" (orange)
    - response.getResponseTimeMs() → "Time: Xms"
    - response.isDatabaseAccessed() → "DB: Yes" or "DB: No"
    ↓
If response.isSuccessful():
    - Get QueryResult from response.getResult()
    - Extract columns: result.getColumns()
    - Extract rows: result.getRows()
    - Update JTable with new data
Else:
    - Show error dialog with response.getErrorMessage()
```

### What You Call

```java
// You receive this from MainFrame (passed in constructor)
private RequestHandler requestHandler;

// When user clicks Execute
String sql = sqlTextArea.getText().trim();
QueryResponse response = requestHandler.executeQuery(sql);

// Check if successful
if (response.isSuccessful()) {
    QueryResult result = response.getResult();
    List<String> columns = result.getColumns();
    List<List<Object>> rows = result.getRows();
    
    // Update table
    updateResultsTable(columns, rows);
    
    // Update status
    statusLabel.setText("Status: " + (response.isCacheHit() ? "CACHE HIT" : "CACHE MISS"));
    timeLabel.setText("Time: " + response.getResponseTimeMs() + "ms");
    dbLabel.setText("DB: " + (response.isDatabaseAccessed() ? "Yes" : "No"));
} else {
    // Show error
    JOptionPane.showMessageDialog(this, response.getErrorMessage(), "Error", JOptionPane.ERROR_MESSAGE);
}
```

---

## Tab 2: Benchmark Panel

**File**: `src/main/java/com/dbcache/ui/panel/BenchmarkPanel.java`

### Components

```
┌─────────────────────────────────────────────────────────┐
│  Benchmark Configuration                                │
│                                                         │
│  Total Requests:    [JSpinner - 10000]                  │
│  Unique Queries:    [JSpinner - 1000]                   │
│  Cache Capacity:    [JSpinner - 100]                    │
│  TTL (seconds):     [JSpinner - 60]                     │
│                                                         │
│  [Run Benchmark]                                        │
│                                                         │
│  ┌───────────────────────────────────────────────────┐ │
│  │ Results                                           │ │
│  │                                                   │ │
│  │ ┌──────────────┬────────────┬────────────────┐   │ │
│  │ │ Metric       │ No Cache   │ With Cache     │   │ │
│  │ ├──────────────┼────────────┼────────────────┤   │ │
│  │ │ Requests     │ 10000      │ 10000          │   │ │
│  │ │ DB Queries   │ 10000      │ 2150           │   │ │
│  │ │ Cache Hits   │ —          │ 7850           │   │ │
│  │ │ Cache Misses │ —          │ 2150           │   │ │
│  │ │ Hit Rate     │ —          │ 78.5%          │   │ │
│  │ │ Total Time   │ 8.4s       │ 2.2s           │   │ │
│  │ │ Avg Latency  │ 0.84ms     │ 0.22ms         │   │ │
│  │ └──────────────┴────────────┴────────────────┘   │ │
│  │                                                   │ │
│  │ Speedup: 3.82x                                    │ │
│  │ DB Load Reduction: 78.5%                          │ │
│  └───────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### Component Details

#### 1. Configuration Form
- **Layout**: `GridBagLayout` or `GridLayout(4, 2)`
- **Components**:
  - Total Requests: `JSpinner` (min: 100, max: 100000, step: 100, default: 10000)
  - Unique Queries: `JSpinner` (min: 10, max: 10000, step: 10, default: 1000)
  - Cache Capacity: `JSpinner` (min: 10, max: 1000, step: 10, default: 100)
  - TTL (seconds): `JSpinner` (min: 1, max: 3600, step: 1, default: 60)

#### 2. Run Button
- **Type**: `JButton`
- **Text**: "Run Benchmark"
- **Action**: Call benchmark runner
- **Disabled state**: While benchmark is running

#### 3. Results Table
- **Type**: `JTable` inside `JScrollPane`
- **Fixed columns**: 3 (Metric, No Cache, With Cache)
- **Fixed rows**: 7 (Requests, DB Queries, Cache Hits, Cache Misses, Hit Rate, Total Time, Avg Latency)
- **Editable**: No
- **Font**: Monospace, 12pt

#### 4. Summary Labels
- **Speedup Label**: `JLabel` - "Speedup: 3.82x"
- **DB Load Reduction Label**: `JLabel` - "DB Load Reduction: 78.5%"
- **Font**: Bold, 14pt

### Data Flow

```
User clicks "Run Benchmark"
    ↓
Get config values from spinners
    ↓
Create BenchmarkConfig:
    new BenchmarkConfig(totalRequests, uniqueQueries, cacheCapacity, ttlSeconds)
    ↓
Call: BenchmarkResults results = benchmarkRunner.runBenchmark(config);
    ↓
Extract metrics:
    BenchmarkMetrics noCacheMetrics = results.getNoCacheMetrics();
    BenchmarkMetrics withCacheMetrics = results.getWithCacheMetrics();
    ↓
Update results table:
    - Row 1: Requests (both should be same)
    - Row 2: DB Queries (noCache.dbQueries vs withCache.dbQueries)
    - Row 3: Cache Hits ("—" vs withCache.cacheHits)
    - Row 4: Cache Misses ("—" vs withCache.cacheMisses)
    - Row 5: Hit Rate ("—" vs withCache.hitRate formatted as %)
    - Row 6: Total Time (noCache.totalTimeMs vs withCache.totalTimeMs formatted as seconds)
    - Row 7: Avg Latency (noCache.avgLatencyMs vs withCache.avgLatencyMs)
    ↓
Calculate and display:
    - Speedup = noCache.totalTimeMs / withCache.totalTimeMs
    - DB Load Reduction = 1 - (withCache.dbQueries / noCache.dbQueries)
```

### What You Call

```java
// You receive this from MainFrame (passed in constructor)
private BenchmarkRunner benchmarkRunner;

// When user clicks Run Benchmark
int totalRequests = (Integer) totalRequestsSpinner.getValue();
int uniqueQueries = (Integer) uniqueQueriesSpinner.getValue();
int cacheCapacity = (Integer) cacheCapacitySpinner.getValue();
long ttlSeconds = (Integer) ttlSpinner.getValue();

BenchmarkConfig config = new BenchmarkConfig(totalRequests, uniqueQueries, cacheCapacity, ttlSeconds);

// Run benchmark (this may take time)
BenchmarkResults results = benchmarkRunner.runBenchmark(config);

// Extract metrics
BenchmarkMetrics noCache = results.getNoCacheMetrics();
BenchmarkMetrics withCache = results.getWithCacheMetrics();

// Update table
Object[][] tableData = {
    {"Requests", noCache.getTotalRequests(), withCache.getTotalRequests()},
    {"DB Queries", noCache.getDbQueries(), withCache.getDbQueries()},
    {"Cache Hits", "—", withCache.getCacheHits()},
    {"Cache Misses", "—", withCache.getCacheMisses()},
    {"Hit Rate", "—", String.format("%.1f%%", withCache.getHitRate() * 100)},
    {"Total Time", formatTime(noCache.getTotalTimeMs()), formatTime(withCache.getTotalTimeMs())},
    {"Avg Latency", formatLatency(noCache.getAvgLatencyMs()), formatLatency(withCache.getAvgLatencyMs())}
};

// Update summary labels
double speedup = (double) noCache.getTotalTimeMs() / withCache.getTotalTimeMs();
double dbLoadReduction = 1.0 - ((double) withCache.getDbQueries() / noCache.getDbQueries());

speedupLabel.setText(String.format("Speedup: %.2fx", speedup));
dbLoadReductionLabel.setText(String.format("DB Load Reduction: %.1f%%", dbLoadReduction * 100));
```

### Helper Methods You Need

```java
private String formatTime(long ms) {
    if (ms < 1000) {
        return ms + "ms";
    } else {
        return String.format("%.1fs", ms / 1000.0);
    }
}

private String formatLatency(double ms) {
    return String.format("%.2fms", ms);
}
```

---

## Tab 3: Cache Stats Panel

**File**: `src/main/java/com/dbcache/ui/panel/CacheStatsPanel.java`

### Components

```
┌─────────────────────────────────────────────────────────┐
│  Cache Statistics                                       │
│                                                         │
│  Size:        45 / 100                                  │
│  Hits:        7850                                      │
│  Misses:      2150                                      │
│  Hit Rate:    78.5%                                     │
│  Evictions:   12                                        │
│                                                         │
│  [Clear Cache]  [Refresh]                               │
└─────────────────────────────────────────────────────────┘
```

### Component Details

#### 1. Stats Labels
- **Layout**: `GridLayout(5, 2)`
- **Components**: 5 pairs of (label, value)
  - Size: "45 / 100"
  - Hits: "7850"
  - Misses: "2150"
  - Hit Rate: "78.5%"
  - Evictions: "12"
- **Font**: 14pt

#### 2. Buttons
- **Clear Cache**: `JButton`
  - Action: Call `requestHandler.clearCache()`
  - Confirmation dialog: "Are you sure you want to clear the cache?"
- **Refresh**: `JButton`
  - Action: Call `requestHandler.getCacheStats()` and update labels

### Data Flow

```
Panel becomes visible OR user clicks "Refresh"
    ↓
Call: CacheStats stats = requestHandler.getCacheStats();
    ↓
Update labels:
    - sizeLabel.setText(stats.getSize() + " / " + stats.getCapacity());
    - hitsLabel.setText(String.valueOf(stats.getHits()));
    - missesLabel.setText(String.valueOf(stats.getMisses()));
    - hitRateLabel.setText(String.format("%.1f%%", stats.getHitRate() * 100));
    - evictionsLabel.setText(String.valueOf(stats.getEvictions()));

User clicks "Clear Cache"
    ↓
Show confirmation dialog
    ↓
If confirmed:
    requestHandler.clearCache();
    Refresh stats
```

### What You Call

```java
// You receive this from MainFrame (passed in constructor)
private RequestHandler requestHandler;

// When panel becomes visible or user clicks Refresh
CacheStats stats = requestHandler.getCacheStats();

sizeLabel.setText(stats.getSize() + " / " + stats.getCapacity());
hitsLabel.setText(String.valueOf(stats.getHits()));
missesLabel.setText(String.valueOf(stats.getMisses()));
hitRateLabel.setText(String.format("%.1f%%", stats.getHitRate() * 100));
evictionsLabel.setText(String.valueOf(stats.getEvictions()));

// When user clicks Clear Cache
int confirm = JOptionPane.showConfirmDialog(
    this, 
    "Are you sure you want to clear the cache?", 
    "Confirm", 
    JOptionPane.YES_NO_OPTION
);

if (confirm == JOptionPane.YES_OPTION) {
    requestHandler.clearCache();
    refreshStats(); // Call the refresh logic above
}
```

---

## Data Structures You'll Work With

These classes already exist. You just need to call their methods.

### QueryResponse
**File**: `src/main/java/com/dbcache/model/QueryResponse.java`

```java
public class QueryResponse {
    boolean isSuccessful();           // true if query succeeded
    QueryResult getResult();          // the query result (null if error)
    boolean isCacheHit();             // true if cache hit
    long getResponseTimeMs();         // response time in milliseconds
    boolean isDatabaseAccessed();     // true if database was queried
    String getErrorMessage();         // error message (null if success)
}
```

### QueryResult
**File**: `src/main/java/com/dbcache/database/QueryResult.java`

```java
public class QueryResult {
    List<String> getColumns();        // column names
    List<List<Object>> getRows();     // row data
    int getRowCount();                // number of rows
    int getColumnCount();             // number of columns
}
```

### CacheStats
**File**: `src/main/java/com/dbcache/cache/CacheStats.java`

```java
public class CacheStats {
    int getSize();                    // current number of entries
    int getCapacity();                // maximum capacity
    long getHits();                   // total cache hits
    long getMisses();                 // total cache misses
    long getEvictions();              // total evictions
    double getHitRate();              // hit rate (0.0 to 1.0)
}
```

### BenchmarkConfig
**File**: `src/main/java/com/dbcache/benchmark/BenchmarkConfig.java`

```java
public class BenchmarkConfig {
    // Constructor
    BenchmarkConfig(int totalRequests, int uniqueQueries, int cacheCapacity, long ttlSeconds);
    
    // Getters
    int getTotalRequests();
    int getUniqueQueries();
    int getCacheCapacity();
    long getTtlSeconds();
}
```

### BenchmarkResults
**File**: `src/main/java/com/dbcache/benchmark/BenchmarkResults.java`

```java
public class BenchmarkResults {
    BenchmarkMetrics getNoCacheMetrics();
    BenchmarkMetrics getWithCacheMetrics();
}
```

### BenchmarkMetrics
**File**: `src/main/java/com/dbcache/benchmark/BenchmarkMetrics.java`

```java
public class BenchmarkMetrics {
    int getTotalRequests();
    int getDbQueries();
    long getCacheHits();
    long getCacheMisses();
    double getHitRate();
    long getTotalTimeMs();
    double getAvgLatencyMs();
}
```

---

## MainFrame Integration

**File**: `src/main/java/com/dbcache/ui/MainFrame.java`

The `MainFrame` class already has a skeleton. You just need to make sure it creates the three tabs correctly.

### What MainFrame Does

```java
public class MainFrame extends JFrame {
    private RequestHandler requestHandler;
    private BenchmarkRunner benchmarkRunner;
    
    public MainFrame(RequestHandler requestHandler, BenchmarkRunner benchmarkRunner) {
        this.requestHandler = requestHandler;
        this.benchmarkRunner = benchmarkRunner;
        
        // Initialize UI
        setTitle("Database Query Cache System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        
        // Create tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manual Query", new ManualQueryPanel(requestHandler));
        tabbedPane.addTab("Benchmark", new BenchmarkPanel(benchmarkRunner));
        tabbedPane.addTab("Cache Stats", new CacheStatsPanel(requestHandler));
        
        add(tabbedPane);
    }
}
```

### Main Method

The `Main.java` file already initializes everything. You don't need to modify it.

```java
public class Main {
    public static void main(String[] args) {
        // Initialize backend (already done for you)
        Config.load();
        DatabaseManager dbManager = new DatabaseManager(
            Config.getDbUrl(),
            Config.getDbUsername(),
            Config.getDbPassword()
        );
        
        CacheConfig cacheConfig = new CacheConfig(
            Config.getCacheCapacity(),
            Config.getCacheTtlSeconds(),
            EvictionPolicy.LRU
        );
        
        RequestHandler requestHandler = new RequestHandler(dbManager, cacheConfig);
        BenchmarkRunner benchmarkRunner = new BenchmarkRunner(requestHandler, dbManager);
        
        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(requestHandler, benchmarkRunner);
            frame.setVisible(true);
        });
    }
}
```

---

## Important Notes

### 1. Thread Safety
- **V1 is single-threaded** — no need for SwingWorker or background threads
- UI will freeze during long operations (benchmarks) — this is acceptable for V1
- All method calls happen on the Event Dispatch Thread (EDT)

### 2. Error Handling
- Always check `response.isSuccessful()` before accessing results
- Show error dialogs for user-friendly error messages
- Log errors to console for debugging

### 3. UI Responsiveness (V1 Limitation)
- Benchmarks may take several seconds
- UI will be unresponsive during benchmark execution
- This is acceptable for V1 (V2 will add background threads)

### 4. Table Updates
- Use `DefaultTableModel` for JTable
- Clear existing data before setting new data
- Example:
  ```java
  DefaultTableModel model = (DefaultTableModel) table.getModel();
  model.setRowCount(0); // Clear existing rows
  model.setColumnIdentifiers(columns.toArray()); // Set column headers
  for (List<Object> row : rows) {
      model.addRow(row.toArray());
  }
  ```

### 5. Formatting
- Percentages: Multiply by 100 and format as "78.5%"
- Time: Show as "ms" if < 1000ms, otherwise as "s" with 1 decimal
- Latency: Always show as "ms" with 2 decimals

---

## Testing Checklist

### Manual Query Panel
- [ ] Execute a valid SELECT query
- [ ] Verify results display in table
- [ ] Verify cache status shows correctly (HIT/MISS)
- [ ] Verify response time displays
- [ ] Verify database access indicator
- [ ] Execute same query twice → second should be CACHE HIT
- [ ] Execute invalid query (e.g., INSERT) → error dialog
- [ ] Clear button works

### Benchmark Panel
- [ ] Run benchmark with default settings
- [ ] Verify results table populates
- [ ] Verify speedup calculation
- [ ] Verify DB load reduction calculation
- [ ] Run with different configurations
- [ ] Verify "No Cache" column shows correct values
- [ ] Verify "With Cache" column shows correct values

### Cache Stats Panel
- [ ] Verify stats display correctly
- [ ] Click Refresh → stats update
- [ ] Click Clear Cache → confirmation dialog
- [ ] After clearing, stats reset to 0
- [ ] Execute some queries → stats update

### Integration
- [ ] All three tabs work independently
- [ ] Switching between tabs doesn't lose state
- [ ] Application closes cleanly

---

## Questions?

If you need clarification on:
- What a method returns → Check the data structures section
- How to update the UI → Check the data flow diagrams
- What to call → Check the "What You Call" sections

The backend team has implemented all the classes mentioned in this guide. You just need to fill in the UI skeleton files and call those methods.

Good luck!
