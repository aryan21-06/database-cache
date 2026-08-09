# Development Guidelines - Database Query Cache System

This document contains rules and guidelines that **all team members** must follow.

---

## Git Workflow

### Branch Strategy

**NEVER push directly to `main` branch.**

Each team member works on their own branch:

```bash
# Create your branch (do this once)
git checkout -b your-name/feature-name

# Example branches:
# - john/cache-engine
# - jane/frontend-ui
# - bob/database-jdbc
# - alice/benchmark-system
```

### Daily Workflow

```bash
# 1. Start your day - pull latest changes from main
git checkout main
git pull origin main

# 2. Switch to your branch
git checkout your-name/feature-name

# 3. Do your work...

# 4. Stage your changes
git add .

# 5. Commit with a clear message
git commit -m "Add LRU eviction logic to cache engine"

# 6. Push to your branch
git push origin your-name/feature-name
```

### Commit Message Format

Use clear, descriptive commit messages:

**Good:**
```
Add LRU eviction logic to cache engine
Fix null pointer exception in QueryResult
Update benchmark configuration UI
Add unit tests for cache hit/miss scenarios
```

**Bad:**
```
fixed stuff
update
wip
asdfasdf
```

### Merging to Main

**Only the project manager merges to main.**

When your feature is complete and tested:
1. Push your branch
2. Tell the project manager
3. Project manager will review and merge

### Resolving Merge Conflicts

If you get merge conflicts:

```bash
# 1. Pull latest main
git checkout main
git pull origin main

# 2. Switch back to your branch
git checkout your-name/feature-name

# 3. Merge main into your branch
git merge main

# 4. Resolve conflicts in your IDE
# 5. Stage resolved files
git add .

# 6. Complete the merge
git commit -m "Merge main and resolve conflicts"

# 7. Push
git push origin your-name/feature-name
```

---

## Java Version

### Required Version: Java 17

All code must be compatible with **Java 17**.

### Check Your Version

```bash
java -version
```

Should show: `openjdk version "17.x.x"` or higher.

### If You Have a Newer Version (Java 21, 25, etc.)

That's fine! The project is configured to compile for Java 17 compatibility.

Just make sure your `pom.xml` has:
```xml
<properties>
    <maven.compiler.release>17</maven.compiler.release>
</properties>
```

### What You CANNOT Use

Since we target Java 17, you **cannot use** features from Java 18+:
- ❌ Record patterns (Java 19)
- ❌ Pattern matching for switch (Java 21)
- ❌ String templates (Java 23)
- ❌ Unnamed variables (Java 22)

### What You CAN Use

Java 17 features are fine:
- ✅ Records (Java 16)
- ✅ Pattern matching for instanceof (Java 16)
- ✅ Sealed classes (Java 17)
- ✅ Text blocks (Java 15)
- ✅ Switch expressions (Java 14)

---

## Maven Commands

### Build the Project

```bash
# Clean and build
mvn clean install

# Or use the wrapper (recommended)
./mvnw clean install        # Linux/Mac
mvnw.cmd clean install      # Windows
```

### Run the Application

```bash
mvn exec:java

# Or with wrapper
./mvnw exec:java            # Linux/Mac
mvnw.cmd exec:java          # Windows
```

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=LRUCacheTest

# Run specific test method
mvn test -Dtest=LRUCacheTest#testCacheHit
```

### Check for Compilation Errors

```bash
mvn compile
```

### Package the Application

```bash
mvn package
```

This creates a JAR file in `target/` directory.

### Common Maven Errors

**Error: "JAVA_HOME not set"**
```bash
# Linux/Mac
export JAVA_HOME=/path/to/jdk-17

# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-17
```

**Error: "Plugin execution not covered by lifecycle configuration"**
```bash
# Just run clean install
mvn clean install
```

**Error: "Could not resolve dependencies"**
```bash
# Force update dependencies
mvn clean install -U
```

---

## Project Structure

```
database-cache/
├── src/main/java/com/dbcache/
│   ├── Main.java                    ← Entry point
│   ├── config/                      ← Configuration loader
│   ├── cache/                       ← Cache engine (your code here)
│   ├── database/                    ← JDBC layer
│   ├── handler/                     ← Request handler
│   ├── benchmark/                   ← Benchmark system
│   ├── model/                       ← Data models
│   └── ui/                          ← Swing UI
├── src/test/java/com/dbcache/       ← Unit tests
├── sql/                             ← Database scripts
├── docs/                            ← Documentation
├── pom.xml                          ← Maven config
└── config.properties                ← Your local config (NOT in git)
```

### Where to Put Your Code

- **Cache team**: `src/main/java/com/dbcache/cache/`
- **Database team**: `src/main/java/com/dbcache/database/`
- **Frontend team**: `src/main/java/com/dbcache/ui/`
- **Benchmark team**: `src/main/java/com/dbcache/benchmark/`

**DO NOT modify files outside your area without asking.**

---

## Code Style

### Naming Conventions

**Classes**: PascalCase
```java
public class LRUCache { }
public class QueryResult { }
```

**Methods**: camelCase
```java
public QueryResult get(String sql) { }
public void executeQuery(String sql) { }
```

**Variables**: camelCase
```java
int cacheSize = 100;
String queryText = "SELECT * FROM students";
```

**Constants**: UPPER_SNAKE_CASE
```java
public static final int MAX_CAPACITY = 1000;
public static final String DEFAULT_TTL = "60";
```

**Packages**: all lowercase
```java
package com.dbcache.cache;
package com.dbcache.database;
```

### Formatting

- **Indentation**: 4 spaces (not tabs)
- **Line length**: Max 120 characters
- **Braces**: Opening brace on same line

```java
// Good
public class LRUCache {
    public QueryResult get(String sql) {
        if (sql == null) {
            return null;
        }
        // ...
    }
}

// Bad
public class LRUCache
{
    public QueryResult get(String sql)
    {
        if (sql == null)
        {
            return null;
        }
    }
}
```

### Comments

**DO** write comments for:
- Complex logic
- Public API methods (Javadoc)
- TODO items

```java
/**
 * Retrieve a cached query result.
 * 
 * @param sql The SQL query (will be normalized)
 * @return The cached QueryResult, or null if not found/expired
 */
public QueryResult get(String sql) {
    // Normalize query to handle case/whitespace differences
    String key = normalizeQuery(sql);
    
    // Check if entry exists and is not expired
    CacheEntry entry = cache.get(key);
    if (entry != null && isExpired(entry)) {
        cache.remove(key);
        return null;
    }
    
    return entry != null ? entry.getResult() : null;
}
```

**DON'T** write obvious comments:
```java
// Bad: This is obvious
int x = 5; // Set x to 5

// Bad: Commented-out code
// int oldCalculation = complexLogic();
// int newCalculation = betterLogic();
```

### Imports

- Use specific imports, not wildcards
- Order: java.*, javax.*, org.*, com.*

```java
// Good
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

// Bad
import java.util.*;
```

---

## Testing Requirements

### Write Unit Tests

Every component must have unit tests in `src/test/java/`.

**Minimum coverage:**
- All public methods
- Edge cases (null, empty, boundary values)
- Error conditions

### Test Naming

```java
@Test
void testCacheHit() { }

@Test
void testCacheMiss() { }

@Test
void testLRUEviction() { }

@Test
void testTTLExpiration() { }
```

### Run Tests Before Pushing

```bash
# Always run tests before pushing
mvn test

# If tests fail, fix them before pushing
```

**NEVER push code that breaks tests.**

---

## Configuration

### Local Configuration

Create your own `config.properties` file:

```bash
cp config.properties.example config.properties
```

Edit `config.properties` with your database credentials:

```properties
db.url=jdbc:mysql://localhost:3306/dbcache
db.username=root
db.password=YOUR_PASSWORD_HERE

cache.capacity=100
cache.ttl.seconds=60
cache.policy=LRU
```

**IMPORTANT**: `config.properties` is in `.gitignore` — it will NOT be committed to git.

### Never Commit Secrets

**DO NOT commit:**
- Database passwords
- API keys
- Personal configuration

**DO commit:**
- Code
- Tests
- Documentation
- `config.properties.example` (template file)

---

## Database Setup

### Initial Setup

See `docs/DATABASE_GUIDE.md` for detailed instructions.

Quick summary:
```bash
# 1. Install MySQL
# 2. Create database
mysql -u root -p < sql/schema.sql

# 3. Load sample data
mysql -u root -p < sql/seed.sql

# 4. Configure config.properties
cp config.properties.example config.properties
# Edit with your password
```

### Verify Database

```sql
USE dbcache;
SELECT COUNT(*) FROM students;  -- Should be 1000
SELECT COUNT(*) FROM courses;   -- Should be 100
```

---

## Running the Application

### Prerequisites

1. Java 17+ installed
2. Maven installed
3. MySQL running with database set up
4. `config.properties` configured

### Run Command

```bash
mvn clean install
mvn exec:java
```

The Swing UI should open.

### Troubleshooting

**Error: "Can't connect to MySQL"**
- Check MySQL is running
- Verify `config.properties` has correct credentials
- Test connection: `mysql -u root -p`

**Error: "Table doesn't exist"**
- Run `sql/schema.sql`
- Run `sql/seed.sql`

**Error: "java.sql.SQLException: Access denied"**
- Wrong password in `config.properties`
- Update with correct MySQL password

---

## Communication

### When You're Stuck

1. Check the documentation in `docs/`
2. Search Google/StackOverflow
3. Ask in the group chat
4. Ask the project manager

### When You Finish a Feature

1. Write/update tests
2. Run all tests: `mvn test`
3. Update documentation if needed
4. Push to your branch
5. Tell the project manager

### When You Find a Bug

1. Check if it's in your code or someone else's
2. If it's yours, fix it
3. If it's someone else's, tell them
4. If unsure, tell the project manager

---

## Code Review Checklist

Before pushing your code, verify:

- [ ] Code compiles: `mvn compile`
- [ ] Tests pass: `mvn test`
- [ ] No hardcoded passwords or secrets
- [ ] `config.properties` is NOT committed
- [ ] Commit messages are clear
- [ ] Code follows naming conventions
- [ ] Complex logic has comments
- [ ] Public methods have Javadoc
- [ ] No unnecessary commented-out code
- [ ] No `System.out.println` for debugging (use logging or remove)

---

## Common Commands Cheat Sheet

### Git

```bash
git status                    # Check what's changed
git add .                     # Stage all changes
git commit -m "message"       # Commit
git push origin branch-name   # Push to branch
git pull origin main          # Pull latest main
git log --oneline -10         # See recent commits
git diff                      # See unstaged changes
```

### Maven

```bash
mvn clean install             # Clean and build
mvn compile                   # Compile only
mvn test                      # Run tests
mvn exec:java                 # Run application
mvn package                   # Create JAR
```

### MySQL

```bash
mysql -u root -p              # Connect to MySQL
USE dbcache;                  # Select database
SHOW TABLES;                  # List tables
SELECT * FROM students;       # Query data
```

---

## Final Rules

1. **NEVER push to main** — always use your branch
2. **ALWAYS run tests** before pushing
3. **NEVER commit secrets** — use `config.properties`
4. **ALWAYS pull before starting work** — stay up to date
5. **ASK for help** if you're stuck
6. **COMMUNICATE** — tell the team what you're working on
7. **DOCUMENT** your code and decisions
8. **TEST** your code thoroughly

---

## Questions?

If anything in this document is unclear:
1. Ask in the group chat
2. Ask the project manager
3. Suggest improvements to this document

Good luck, and let's build something awesome!
