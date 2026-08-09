# Database Query Cache System

A Java desktop application demonstrating how an in-memory cache reduces database query load and improves response time.

## Features

- Manual query execution with cache hit/miss visualization
- Benchmark system to measure cache performance
- LRU cache with configurable capacity and TTL
- Comprehensive metrics and statistics

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+

## Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd database-cache
   ```

2. **Setup database**
   ```bash
   mysql -u root -p < sql/schema.sql
   mysql -u root -p < sql/seed.sql
   ```

3. **Configure application**
   ```bash
   cp config.properties.example config.properties
   # Edit config.properties with your database credentials
   ```

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn exec:java
   ```

## Project Structure

```
database-cache/
├── src/main/java/com/dbcache/
│   ├── Main.java                    # Application entry point
│   ├── config/
│   │   └── Config.java              # Configuration loader
│   ├── cache/
│   │   ├── CacheEngine.java         # Cache interface
│   │   ├── LRUCache.java            # LRU implementation
│   │   ├── CacheEntry.java          # Cache entry with metadata
│   │   ├── CacheConfig.java         # Cache configuration
│   │   ├── CacheStats.java          # Cache statistics
│   │   └── EvictionPolicy.java      # Eviction policy enum
│   ├── database/
│   │   ├── DatabaseManager.java     # JDBC operations
│   │   └── QueryResult.java         # Materialized query result
│   ├── handler/
│   │   └── RequestHandler.java      # Query orchestration
│   ├── benchmark/
│   │   ├── BenchmarkRunner.java     # Benchmark execution
│   │   ├── WorkloadGenerator.java   # Workload generation
│   │   ├── QueryBuilder.java        # Query template builder
│   │   ├── QueryTemplates.java      # Query templates
│   │   ├── ParameterRanges.java     # Parameter ranges
│   │   ├── DistributionEngine.java  # Distribution logic
│   │   ├── BenchmarkConfig.java     # Benchmark configuration
│   │   └── BenchmarkMetrics.java    # Benchmark metrics
│   ├── model/
│   │   ├── QueryResponse.java       # Query response wrapper
│   │   └── QueryValidator.java      # Query validation
│   └── ui/
│       ├── MainFrame.java           # Main application window
│       ├── panel/
│       │   ├── ManualQueryPanel.java    # Manual query tab
│       │   ├── BenchmarkPanel.java      # Benchmark tab
│       │   └── CacheStatsPanel.java     # Cache stats tab
│       └── component/
│           └── ResultsTable.java    # Reusable table component
├── src/main/resources/
├── src/test/java/com/dbcache/
│   ├── cache/
│   │   └── LRUCacheTest.java
│   ├── database/
│   │   └── DatabaseManagerTest.java
│   └── benchmark/
│       └── WorkloadGeneratorTest.java
├── sql/
│   ├── schema.sql
│   └── seed.sql
├── docs/
│   ├── ARCHITECTURE.md
│   └── FRONTEND_GUIDE.md
├── pom.xml
├── config.properties.example
├── .gitignore
└── README.md
```

## Documentation

- [Architecture Document](docs/ARCHITECTURE.md) - Complete system architecture and design
- [Frontend Developer Guide](docs/FRONTEND_GUIDE.md) - Guide for UI developers

## Team

- Project Manager: [Your Name]
- Cache Engine: [Team Member]
- Frontend (Swing UI): [Team Member]
- Database & Benchmark: [Team Member]

## License

This is a student project for educational purposes.
