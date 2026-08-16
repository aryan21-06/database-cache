# Database Developer Guide - Database Query Cache System

## What You'll Do

You are responsible for two things:

1. **Setting up the MySQL database** (Part 1)

   - Installing MySQL on your computer
   - Creating the database
   - Running the SQL scripts to create tables
   - Adding sample data
   - Making sure everything works
2. **Implementing the JDBC layer** (Part 2)

   - Understanding how Java connects to MySQL
   - Implementing the `DatabaseManager` class
   - Testing that Java can query the database
   - Making sure the `QueryResult` class works correctly

Don't worry if you're new to databases or JDBC — this guide will walk you through everything step by step.

---

# PART 1: DATABASE SETUP

---

## What is MySQL?

**MySQL** is a database system. Think of it like a spreadsheet, but much more powerful:

- **Spreadsheet**: Stores data in rows and columns (one file)
- **Database**: Stores data in tables (like multiple spreadsheets), can handle millions of rows, multiple users at once

Our application will:

- **Store data** in MySQL (students, courses, enrollments, etc.)
- **Query data** using SQL (SELECT statements)
- **Cache results** in Java memory to avoid querying MySQL every time

You don't need to understand all of this — you just need to get MySQL running and create the tables.

---

## Step 1: Install MySQL

### For Windows

**Option A: MySQL Installer (Recommended)**

1. Go to https://dev.mysql.com/downloads/installer/
2. Download **MySQL Installer for Windows** (the larger file, ~300MB)
3. Run the installer
4. Choose **Developer Default** setup type
5. When asked for a root password, **use a simple password** like `password123` (this is just for development)
6. **Write down the password** — you'll need it later
7. Finish the installation

**Option B: Using Chocolatey (if you have it)**

Open PowerShell as Administrator and run:

```powershell
choco install mysql
```

### For Linux (Ubuntu/Debian)

Open terminal and run:

```bash
sudo apt update
sudo apt install mysql-server
```

During installation, it will ask for a root password. Use something simple like `password123` and **write it down**.

### Verify Installation

**Windows:**
Open Command Prompt and run:

```cmd
mysql --version
```

**Linux:**
Open terminal and run:

```bash
mysql --version
```

You should see something like:

```
mysql  Ver 8.0.36 for Win64 on x86_64 (MySQL Community Server - GPL)
```

If you see an error, MySQL might not be in your PATH. That's okay — we'll use the full path or a GUI tool instead.

---

## Step 2: Start MySQL Service

MySQL needs to be running in the background.

### Windows

MySQL usually starts automatically. To check:

1. Press `Win + R`, type `services.msc`, press Enter
2. Look for **MySQL80** (or similar)
3. Status should say **Running**
4. If not, right-click → **Start**

### Linux

```bash
sudo systemctl start mysql
sudo systemctl enable mysql
```

To check if it's running:

```bash
sudo systemctl status mysql
```

---

## Step 3: Connect to MySQL

### Using Command Line

**Windows:**

```cmd
mysql -u root -p
```

**Linux:**

```bash
mysql -u root -p
```

It will ask for your password. Enter the password you set during installation.

You should see:

```
Welcome to the MySQL monitor.  Commands end with ; or \g.
mysql>
```

Type `exit` to quit.

### Using a GUI Tool (Easier)

If command line is confusing, use a GUI tool:

**Option A: MySQL Workbench (Recommended)**

- Download: https://dev.mysql.com/downloads/workbench/
- Open MySQL Workbench
- Click the **localhost** connection (it should already be there)
- Enter your root password

**Option B: HeidiSQL (Windows only, lightweight)**

- Download: https://www.heidisql.com/
- Open HeidiSQL
- Hostname: `localhost`
- User: `root`
- Password: your password
- Click **Open**

---

## Step 4: Create the Database

Now we'll create the database and tables.

### Using Command Line

1. Connect to MySQL:

   ```bash
   mysql -u root -p
   ```
2. Run the schema script:

   ```sql
   source /path/to/database-cache/sql/schema.sql
   ```

   **Windows example:**

   ```sql
   source C:/Users/YourName/database-cache/sql/schema.sql
   ```

   **Linux example:**

   ```sql
   source /home/yourname/database-cache/sql/schema.sql
   ```
3. Run the seed script:

   ```sql
   source /path/to/database-cache/sql/seed.sql
   ```

### Using MySQL Workbench

1. Open MySQL Workbench and connect to localhost
2. Click **File** → **Open SQL Script**
3. Navigate to `sql/schema.sql` and open it
4. Click the **Execute** button (lightning bolt icon) or press `Ctrl + Shift + Enter`
5. Repeat for `sql/seed.sql`

### Using HeidiSQL

1. Open HeidiSQL and connect
2. Click **File** → **Open SQL file**
3. Select `sql/schema.sql`
4. Press `F9` to execute
5. Repeat for `sql/seed.sql`

### Manual Method (if scripts don't work)

If the scripts don't work, you can run the commands manually.

**Create database:**

```sql
CREATE DATABASE IF NOT EXISTS dbcache;
USE dbcache;
```

**Create tables:**

```sql
CREATE TABLE departments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    head VARCHAR(100)
);

CREATE TABLE students (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    department_id INT,
    year INT,
    gpa DECIMAL(3,2),
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE TABLE courses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    credits INT,
    department_id INT,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

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

**Insert sample data:**

Run the `sql/seed.sql` file. It contains stored procedures that generate:

- 5 departments
- 100 courses
- 1000 students
- 2000 enrollments

If the seed script doesn't work, you can manually insert a few rows for testing:

```sql
INSERT INTO departments (name, head) VALUES
('Computer Science', 'Dr. Smith'),
('Mathematics', 'Dr. Johnson'),
('Physics', 'Dr. Williams'),
('Chemistry', 'Dr. Brown'),
('Biology', 'Dr. Davis');

INSERT INTO courses (title, credits, department_id) VALUES
('Introduction to Programming', 3, 1),
('Data Structures', 4, 1),
('Calculus I', 4, 2);

INSERT INTO students (name, email, department_id, year, gpa) VALUES
('John Doe', 'john.doe@example.com', 1, 2, 3.5),
('Jane Smith', 'jane.smith@example.com', 1, 3, 3.8);

INSERT INTO enrollments (student_id, course_id, semester, grade) VALUES
(1, 1, 'Fall 2023', 'A'),
(1, 2, 'Fall 2023', 'B');
```

---

## Step 5: Verify Everything Works

Run these queries to check if data was inserted correctly:

```sql
USE dbcache;

-- Verify row counts
SELECT COUNT(*) AS department_count FROM departments;  -- Should be 5
SELECT COUNT(*) AS course_count FROM courses;           -- Should be 100
SELECT COUNT(*) AS student_count FROM students;         -- Should be 1000
SELECT COUNT(*) AS enrollment_count FROM enrollments;   -- Should be 2000
```

Try a few more queries:

```sql
-- See all departments
SELECT * FROM departments;

-- See first 10 courses
SELECT * FROM courses LIMIT 10;

-- See first 10 students
SELECT * FROM students LIMIT 10;

-- See first 10 enrollments
SELECT * FROM enrollments LIMIT 10;

-- Count students per department
SELECT department_id, COUNT(*) as count 
FROM students 
GROUP BY department_id;
```

If all queries work and counts are correct, you're done with Part 1!

---

## Step 6: Configure the Application

The application needs to know how to connect to your database.

1. Copy the example config file:

   ```bash
   cp config.properties.example config.properties
   ```
2. Open `config.properties` in a text editor
3. Update the database settings:

   ```properties
   # Database connection
   db.url=jdbc:mysql://localhost:3306/dbcache
   db.username=root
   db.password=YOUR_PASSWORD_HERE
   ```

   Replace `YOUR_PASSWORD_HERE` with your actual MySQL password.
4. Save the file

**Important**: The `config.properties` file is in `.gitignore`, so your password won't be committed to git.

---

## Understanding the Database Schema

Let's understand what we just created.

### Tables and Relationships

```
departments (5 rows)
    │
    ├── id (primary key)
    ├── name (e.g., "Computer Science")
    └── head (e.g., "Dr. Smith")
    │
    └──< students (1000 rows)
            │
            ├── id (primary key)
            ├── name
            ├── email
            ├── department_id (foreign key → departments.id)
            ├── year (1-4)
            └── gpa (0.0-4.0)
            │
            └──< enrollments (2000 rows)
                    │
                    ├── id (primary key)
                    ├── student_id (foreign key → students.id)
                    ├── course_id (foreign key → courses.id)
                    ├── semester (e.g., "Fall 2023")
                    └── grade (A, B, C, D, F)
                    │
                    └──> courses (100 rows)
                            │
                            ├── id (primary key)
                            ├── title
                            ├── credits (1-6)
                            └── department_id (foreign key → departments.id)
```

### What This Means

- **departments**: 5 departments (CS, Math, Physics, Chemistry, Biology)
- **students**: 1000 students, each belongs to one department
- **courses**: 100 courses, each belongs to one department
- **enrollments**: 2000 enrollment records (which student took which course)

### Example Queries

```sql
-- Find all students in Computer Science department
SELECT * FROM students WHERE department_id = 1;

-- Find all courses with more than 3 credits
SELECT * FROM courses WHERE credits > 3;

-- Find all enrollments for student ID 1
SELECT * FROM enrollments WHERE student_id = 1;

-- Count students per department
SELECT department_id, COUNT(*) as count 
FROM students 
GROUP BY department_id;
```

---

## Troubleshooting

### Problem: "mysql: command not found"

**Solution**: MySQL is not in your PATH.

**Windows:**

- Use the full path: `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe -u root -p`
- Or use MySQL Workbench instead

**Linux:**

```bash
# Try this
/usr/bin/mysql -u root -p

# Or find where mysql is installed
which mysql
```

### Problem: "Access denied for user 'root'"

**Solution**: Wrong password.

- Try the password you set during installation
- If you forgot it, you'll need to reset the root password (Google: "reset mysql root password")

### Problem: "Can't connect to MySQL server on 'localhost'"

**Solution**: MySQL service is not running.

**Windows:**

- Open Services (`services.msc`)
- Find MySQL80
- Right-click → Start

**Linux:**

```bash
sudo systemctl start mysql
```

### Problem: "Unknown database 'dbcache'"

**Solution**: You haven't created the database yet.

- Run the schema script again (Step 4)
- Or manually: `CREATE DATABASE dbcache;`

### Problem: "Table doesn't exist"

**Solution**: You haven't run the schema script.

- Run `sql/schema.sql` first
- Make sure you're using the `dbcache` database: `USE dbcache;`

### Problem: Scripts don't run in MySQL Workbench

**Solution**: Make sure you're connected to the right database.

- In MySQL Workbench, run `USE dbcache;` first
- Then run the script

---

## What to Tell the Team

Once everything is working, tell the team:

1. **MySQL is installed and running**
2. **Database name**: `dbcache`
3. **Username**: `root`
4. **Password**: (tell them your password, or tell them to use their own)
5. **Host**: `localhost`
6. **Port**: `3306` (default)

They'll need this information to configure their `config.properties` file.

---

## Quick Reference

### Common Commands

```sql
-- Show all databases
SHOW DATABASES;

-- Use a database
USE dbcache;

-- Show all tables in current database
SHOW TABLES;

-- Describe a table (see columns)
DESCRIBE students;

-- Select all data from a table
SELECT * FROM students;

-- Count rows in a table
SELECT COUNT(*) FROM students;
```

### File Locations

- **Schema script**: `sql/schema.sql`
- **Seed script**: `sql/seed.sql`
- **Config file**: `config.properties` (create from `config.properties.example`)

### Connection Details

```
Host: localhost
Port: 3306
Database: dbcache
Username: root
Password: (your password)
```

---

## Summary

**What you did:**

1. ✅ Installed MySQL
2. ✅ Started MySQL service
3. ✅ Created the `dbcache` database
4. ✅ Created tables (departments, students, courses, enrollments)
5. ✅ Added sample data (5 departments, 100 courses, 1000 students, 2000 enrollments)
6. ✅ Verified everything works
7. ✅ Configured `config.properties`

**What happens next:**

- Now you'll implement the JDBC layer (Part 2)
- The application will use your DatabaseManager to query the database
- The cache will store results to avoid repeated queries

**If something breaks:**

- Check the troubleshooting section
- Ask the project manager for help
- Google the error message (most MySQL errors have simple solutions)

---

# PART 2: JDBC AND DATABASEMANAGER

## What is JDBC?

**JDBC** stands for **Java Database Connectivity**. It's a bridge that lets Java talk to databases.

Think of it like this:

- **MySQL** speaks SQL (the database language)
- **Java** speaks Java (the programming language)
- **JDBC** translates between them

Without JDBC, Java can't query MySQL. With JDBC, Java can send SQL queries and get results back.

### How JDBC Works

```
Java Code
    ↓
JDBC (translates)
    ↓
MySQL Database
    ↓
Returns results
    ↓
JDBC (translates back)
    ↓
Java receives ResultSet
    ↓
Convert to QueryResult (our custom class)
```

### Key JDBC Concepts

1. **Connection**: A link between Java and the database

   - Like opening a phone call to the database
   - Needs URL, username, password
2. **Statement**: A SQL query you want to run

   - Like saying something during the phone call
3. **ResultSet**: The results returned by the database

   - Like hearing the answer on the phone
   - Temporary — disappears when connection closes
4. **ResultSetMetaData**: Information about the ResultSet

   - Column names, number of columns, data types

---

## Your Responsibilities

You need to implement these files:

### 1. DatabaseManager.java

**File**: `src/main/java/com/dbcache/database/DatabaseManager.java`

**What it does**:

- Connects to MySQL using JDBC
- Executes SQL queries
- Converts ResultSet to QueryResult
- Manages the database connection

**Key methods you need to implement**:

```java
public DatabaseManager(String url, String username, String password)
public QueryResult executeQuery(String sql)
public void close()
```

### 2. QueryResult.java

**File**: `src/main/java/com/dbcache/database/QueryResult.java`

**What it does**:

- Stores query results in Java objects
- Contains column names and row data
- Independent of JDBC (can be cached)

**Key methods**:

```java
public List<String> getColumns()
public List<List<Object>> getRows()
public int getRowCount()
public int getColumnCount()
```

---

## Understanding the Code

### DatabaseManager.java (Skeleton)

The skeleton file already exists. Here's what each part does:

```java
public class DatabaseManager {
  
    private final Connection connection;  // The JDBC connection
  
    // Constructor: Creates a connection to MySQL
    public DatabaseManager(String url, String username, String password) 
            throws SQLException {
        // DriverManager.getConnection() creates the connection
        // url: "jdbc:mysql://localhost:3306/dbcache"
        // username: "root"
        // password: "yourpassword"
        this.connection = DriverManager.getConnection(url, username, password);
    }
  
    // Execute a query and return results
    public QueryResult executeQuery(String sql) throws SQLException {
        // 1. Create a Statement object
        // 2. Execute the query
        // 3. Get the ResultSet
        // 4. Convert ResultSet to QueryResult
        // 5. Close resources
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
        
            return convertResultSet(rs);
        }
    }
  
    // Convert JDBC ResultSet to our QueryResult
    private QueryResult convertResultSet(ResultSet rs) throws SQLException {
        // 1. Get column names from ResultSetMetaData
        // 2. Loop through rows
        // 3. Extract data from each row
        // 4. Build QueryResult object
    
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
    
        List<String> columns = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();
    
        // TODO: Extract column names
        for (int i = 1; i <= columnCount; i++) {
            columns.add(metaData.getColumnName(i));
        }
    
        // TODO: Extract rows
        while (rs.next()) {
            List<Object> row = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                row.add(rs.getObject(i));
            }
            rows.add(row);
        }
    
        return new QueryResult(columns, rows);
    }
  
    // Close the connection
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
```

### QueryResult.java (Skeleton)

This is simpler — just a data container:

```java
public class QueryResult {
  
    private final List<String> columns;      // Column names
    private final List<List<Object>> rows;   // Row data
  
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

---

## Step-by-Step Implementation

### Step 1: Understand the Connection String

The connection string tells JDBC where to find the database:

```
jdbc:mysql://localhost:3306/dbcache
```

Breaking it down:

- `jdbc:mysql://` → Use MySQL JDBC driver
- `localhost` → Database is on this computer
- `3306` → Default MySQL port
- `dbcache` → Database name

This comes from `config.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/dbcache
db.username=root
db.password=yourpassword
```

### Step 2: Implement DatabaseManager

Open `src/main/java/com/dbcache/database/DatabaseManager.java`

**What to implement**:

1. **Constructor** (already done in skeleton):

   - Creates connection using `DriverManager.getConnection()`
2. **executeQuery()** (already done in skeleton):

   - Creates a Statement
   - Executes the SQL query
   - Gets the ResultSet
   - Calls `convertResultSet()`
3. **convertResultSet()** (you need to complete this):

   - Get column names from `ResultSetMetaData`
   - Loop through rows using `rs.next()`
   - Extract each column value using `rs.getObject(i)`
   - Build the QueryResult
4. **close()** (already done in skeleton):

   - Closes the connection

### Step 3: Implement QueryResult

Open `src/main/java/com/dbcache/database/QueryResult.java`

This is already mostly done. Just make sure:

- Constructor stores columns and rows
- Getters return the correct data
- `getRowCount()` returns `rows.size()`
- `getColumnCount()` returns `columns.size()`

### Step 4: Test the Connection

Create a simple test to verify JDBC works:

```java
public class JdbcTest {
    public static void main(String[] args) {
        try {
            // Load config
            Config.load();
        
            // Create DatabaseManager
            DatabaseManager dbManager = new DatabaseManager(
                Config.getDbUrl(),
                Config.getDbUsername(),
                Config.getDbPassword()
            );
        
            // Execute a query
            QueryResult result = dbManager.executeQuery("SELECT * FROM students LIMIT 5");
        
            // Print results
            System.out.println("Columns: " + result.getColumns());
            System.out.println("Row count: " + result.getRowCount());
        
            for (List<Object> row : result.getRows()) {
                System.out.println(row);
            }
        
            // Close connection
            dbManager.close();
        
            System.out.println("JDBC test passed!");
        
        } catch (Exception e) {
            System.err.println("JDBC test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

**Expected output**:

```
Columns: [id, name, email, department_id, year, gpa]
Row count: 5
[1, John Doe, john.doe@example.com, 1, 2, 3.50]
[2, Jane Smith, jane.smith@example.com, 1, 3, 3.80]
...
JDBC test passed!
```

### Step 5: Test Different Queries

Try various queries to make sure everything works:

```java
// Simple SELECT
dbManager.executeQuery("SELECT * FROM departments");

// SELECT with WHERE
dbManager.executeQuery("SELECT * FROM students WHERE department_id = 1");

// SELECT with JOIN
dbManager.executeQuery(
    "SELECT s.name, c.title FROM students s " +
    "JOIN enrollments e ON s.id = e.student_id " +
    "JOIN courses c ON e.course_id = c.id " +
    "LIMIT 10"
);

// Aggregate query
dbManager.executeQuery("SELECT COUNT(*) FROM students");

// SELECT with GROUP BY
dbManager.executeQuery(
    "SELECT department_id, COUNT(*) as count " +
    "FROM students GROUP BY department_id"
);
```

---

## Common JDBC Errors and Solutions

### Error 1: "No suitable driver found for jdbc:mysql://..."

**Cause**: MySQL JDBC driver is not in the classpath.

**Solution**:

- Check `pom.xml` has the MySQL connector dependency:
  ```xml
  <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <version>8.3.0</version>
  </dependency>
  ```
- Run `mvn clean install` to download the driver

### Error 2: "Communications link failure"

**Cause**: Can't connect to MySQL server.

**Solution**:

- Check MySQL is running (see Part 1, Step 2)
- Check the URL is correct: `jdbc:mysql://localhost:3306/dbcache`
- Check port 3306 is not blocked by firewall

### Error 3: "Access denied for user 'root'@'localhost'"

**Cause**: Wrong username or password.

**Solution**:

- Check `config.properties` has correct username and password
- Test connection manually: `mysql -u root -p`
- If password is wrong, reset it (Google: "reset mysql root password")

### Error 4: "Unknown database 'dbcache'"

**Cause**: Database doesn't exist.

**Solution**:

- Run the schema script again (Part 1, Step 4)
- Or manually: `CREATE DATABASE dbcache;`

### Error 5: "Table 'dbcache.students' doesn't exist"

**Cause**: Tables haven't been created.

**Solution**:

- Run `sql/schema.sql`
- Run `sql/seed.sql`
- Verify with: `SHOW TABLES;` in MySQL

### Error 6: "ResultSet is closed"

**Cause**: Trying to access ResultSet after connection is closed.

**Solution**:

- This is why we convert ResultSet to QueryResult immediately
- Never store ResultSet — always convert it to QueryResult
- QueryResult is independent of the connection

### Error 7: "Column index out of range"

**Cause**: Trying to access a column that doesn't exist.

**Solution**:

- Check column count: `metaData.getColumnCount()`
- Remember: columns are 1-indexed (start at 1, not 0)
- Example: `rs.getObject(1)` gets the first column

---

## Understanding ResultSet

### What is ResultSet?

ResultSet is what JDBC returns after executing a query. It's like a table of data:

```
ResultSet for: SELECT id, name, email FROM students LIMIT 3

┌────┬──────────┬──────────────────────┐
│ id │ name     │ email                │
├────┼──────────┼──────────────────────┤
│ 1  │ John Doe │ john@example.com     │
│ 2  │ Jane Doe │ jane@example.com     │
│ 3  │ Bob Smith│ bob@example.com      │
└────┴──────────┴──────────────────────┘
```

### How to Read ResultSet

```java
ResultSet rs = stmt.executeQuery("SELECT id, name, email FROM students");

// Move to first row
rs.next();  // Returns true if there's a row

// Get column values (1-indexed!)
int id = rs.getInt(1);           // First column
String name = rs.getString(2);   // Second column
String email = rs.getString(3);  // Third column

// Or get by column name
int id = rs.getInt("id");
String name = rs.getString("name");
String email = rs.getString("email");

// Move to next row
rs.next();  // Move to second row

// Loop through all rows
while (rs.next()) {
    // Process each row
    System.out.println(rs.getString("name"));
}
```

### ResultSetMetaData

Metadata = "data about data". ResultSetMetaData tells you about the ResultSet:

```java
ResultSetMetaData metaData = rs.getMetaData();

// How many columns?
int columnCount = metaData.getColumnCount();  // e.g., 3

// What are the column names?
for (int i = 1; i <= columnCount; i++) {
    String columnName = metaData.getColumnName(i);
    System.out.println("Column " + i + ": " + columnName);
}

// What are the data types?
for (int i = 1; i <= columnCount; i++) {
    String typeName = metaData.getColumnTypeName(i);
    System.out.println("Column " + i + " type: " + typeName);
}
```

### Why Convert ResultSet to QueryResult?

ResultSet is tied to the JDBC connection. If you close the connection, ResultSet becomes invalid.

**Problem**:

```java
ResultSet rs = stmt.executeQuery("SELECT * FROM students");
connection.close();  // Close connection
rs.next();  // ERROR! ResultSet is closed
```

**Solution**: Convert ResultSet to QueryResult immediately:

```java
ResultSet rs = stmt.executeQuery("SELECT * FROM students");
QueryResult result = convertResultSet(rs);  // Copy data to QueryResult
connection.close();  // Now safe to close
// result is still valid!
```

QueryResult is a plain Java object — it doesn't depend on the connection.

---

## Testing Your Implementation

### Test 1: Simple Query

```java
DatabaseManager dbManager = new DatabaseManager(url, username, password);
QueryResult result = dbManager.executeQuery("SELECT * FROM departments");

System.out.println("Columns: " + result.getColumns());
// Expected: [id, name, head]

System.out.println("Rows: " + result.getRowCount());
// Expected: 5
```

### Test 2: Query with WHERE

```java
QueryResult result = dbManager.executeQuery(
    "SELECT * FROM students WHERE department_id = 1"
);

System.out.println("Computer Science students: " + result.getRowCount());
// Should be ~200 (1000 students / 5 departments)
```

### Test 3: JOIN Query

```java
QueryResult result = dbManager.executeQuery(
    "SELECT s.name, c.title " +
    "FROM students s " +
    "JOIN enrollments e ON s.id = e.student_id " +
    "JOIN courses c ON e.course_id = c.id " +
    "WHERE s.id = 1"
);

System.out.println("Student 1's courses:");
for (List<Object> row : result.getRows()) {
    System.out.println("  " + row.get(0) + " - " + row.get(1));
}
```

### Test 4: Aggregate Query

```java
QueryResult result = dbManager.executeQuery(
    "SELECT department_id, COUNT(*) as count " +
    "FROM students " +
    "GROUP BY department_id"
);

System.out.println("Students per department:");
for (List<Object> row : result.getRows()) {
    System.out.println("  Dept " + row.get(0) + ": " + row.get(1) + " students");
}
```

### Test 5: Empty Result

```java
QueryResult result = dbManager.executeQuery(
    "SELECT * FROM students WHERE id = 99999"
);

System.out.println("Row count: " + result.getRowCount());
// Expected: 0

System.out.println("Columns: " + result.getColumns());
// Should still have column names even if no rows
```

---

## Integration with Other Components

### How DatabaseManager Fits In

```
User executes query
    ↓
RequestHandler receives query
    ↓
Check cache (CacheEngine)
    ↓
Cache miss? → Call DatabaseManager.executeQuery()
    ↓
DatabaseManager queries MySQL via JDBC
    ↓
Returns QueryResult
    ↓
Store in cache
    ↓
Return to user
```

### What Other Components Expect

**RequestHandler** expects:

- `DatabaseManager.executeQuery(sql)` returns a `QueryResult`
- `QueryResult` has columns and rows
- If query fails, throws `SQLException`

**CacheEngine** expects:

- `QueryResult` is a plain Java object
- Can be stored in a HashMap
- Doesn't depend on JDBC connection

**UI** expects:

- `QueryResult.getColumns()` returns column names
- `QueryResult.getRows()` returns row data
- Can display in JTable

---

## Quick Reference

### JDBC Connection

```java
// Create connection
Connection conn = DriverManager.getConnection(url, username, password);

// URL format
String url = "jdbc:mysql://localhost:3306/dbcache";
```

### Execute Query

```java
// Create statement
Statement stmt = conn.createStatement();

// Execute query
ResultSet rs = stmt.executeQuery(sql);

// Process results
while (rs.next()) {
    Object value = rs.getObject(1);
}

// Close resources
rs.close();
stmt.close();
```

### Try-with-Resources (Recommended)

```java
// Automatically closes resources
try (Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery(sql)) {
  
    while (rs.next()) {
        // Process results
    }
}  // stmt and rs are automatically closed here
```

### Common ResultSet Methods

```java
rs.next()              // Move to next row (returns boolean)
rs.getObject(i)        // Get column i as Object
rs.getString(i)        // Get column i as String
rs.getInt(i)           // Get column i as int
rs.getDouble(i)        // Get column i as double
rs.getColumnName(i)    // Get column name (via metaData)
```

---

## Summary

**What you did:**

1. ✅ Set up MySQL database (Part 1)
2. ✅ Understood what JDBC is
3. ✅ Implemented DatabaseManager
4. ✅ Implemented QueryResult
5. ✅ Tested JDBC connection
6. ✅ Verified queries work correctly

**What happens next:**

- The cache team will use your DatabaseManager
- When there's a cache miss, they'll call `dbManager.executeQuery(sql)`
- Your QueryResult will be stored in the cache

**If something breaks:**

- Check the JDBC troubleshooting section
- Test connection manually with `mysql -u root -p`
- Verify `config.properties` has correct credentials
- Check MySQL is running

Good luck! You've got this.
