# Understanding the `target/` Folder

## What is the `target/` Folder?

The `target/` folder is **Maven's build output directory**. It's automatically created when you run Maven commands like `mvn compile`, `mvn test`, or `mvn package`. Maven uses this folder to store all build artifacts and generated files.

## Why is it Needed?

### 1. **Separation of Concerns**
- Keeps source code (`src/`) separate from compiled code (`target/`)
- Makes it easy to clean builds without affecting source code
- Follows Maven's "convention over configuration" principle

### 2. **Build Artifacts Storage**
- Stores compiled `.class` files
- Keeps test results and reports
- Holds packaged JARs/WARs (if you build them)

### 3. **Easy Cleanup**
- Running `mvn clean` deletes the entire `target/` folder
- Ensures fresh builds without leftover artifacts
- Prevents "works on my machine" problems

## What's Inside `target/`?

### Directory Structure

```
target/
├── classes/                      # Compiled production code (.class files)
├── test-classes/                 # Compiled test code (.class files)
├── generated-sources/            # Auto-generated source files
├── generated-test-sources/       # Auto-generated test source files
├── maven-status/                 # Maven build metadata
├── surefire-reports/             # Test execution reports ⭐ IMPORTANT
└── [your-artifact].jar           # Packaged application (if applicable)
```

### Key Subdirectories Explained

#### 1. **`test-classes/`**
- Contains **compiled test code** (.class files)
- These are the actual bytecode files that JUnit executes
- Generated from `src/test/java/`

**Example:**
```
test-classes/com/ecse429/restapi/
├── BaseApiTest.class
├── CategoriesApiExpectedBehaviorTest.class
├── CategoriesApiActualBehaviorTest.class
└── ...
```

#### 2. **`surefire-reports/`** ⭐ **MOST IMPORTANT FOR YOU**
This folder contains **test execution results**. It's created by the Maven Surefire plugin (which runs JUnit tests).

**Two types of reports for each test class:**

**a) Text Reports (`.txt` files)**
- Human-readable summary
- Shows pass/fail counts
- Includes error messages and stack traces

Example: `com.ecse429.restapi.CategoriesApiExpectedBehaviorTest.txt`
```
-------------------------------------------------------------------------------
Test set: com.ecse429.restapi.CategoriesApiExpectedBehaviorTest
-------------------------------------------------------------------------------
Tests run: 15, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.231 s
```

**b) XML Reports (`TEST-*.xml` files)**
- Machine-readable format
- Used by CI/CD tools (Jenkins, GitHub Actions, etc.)
- Contains detailed timing and failure information

Example: `TEST-com.ecse429.restapi.CategoriesApiExpectedBehaviorTest.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.ecse429.restapi.CategoriesApiExpectedBehaviorTest" 
           tests="15" failures="1" errors="0" skipped="0" time="0.231">
  <testcase name="testGetAllCategories" time="0.023"/>
  <testcase name="testCreateCategory" time="0.019"/>
  ...
</testsuite>
```

#### 3. **`maven-status/`**
- Tracks which files Maven has compiled
- Used by Maven to determine what needs recompiling
- Speeds up incremental builds

#### 4. **`generated-test-sources/`**
- Auto-generated source code for tests
- Typically empty unless you use annotation processors
- Contains generated test annotations

## How Maven Uses `target/`

### Build Lifecycle

1. **`mvn clean`** → Deletes entire `target/` folder
2. **`mvn compile`** → Compiles source code → `target/classes/`
3. **`mvn test-compile`** → Compiles test code → `target/test-classes/`
4. **`mvn test`** → Runs tests → `target/surefire-reports/`
5. **`mvn package`** → Creates JAR/WAR → `target/*.jar`

### Example Flow

```bash
# Start fresh
mvn clean                    # Deletes target/

# Compile tests
mvn test-compile            # Creates target/test-classes/

# Run tests
mvn test                    # Creates target/surefire-reports/
```

## Why is `target/` in `.gitignore`?

The `target/` folder should **NEVER** be committed to Git because:

1. **Generated Files**: Everything in `target/` can be recreated from source code
2. **Large Size**: Contains compiled bytecode, making repo bloated
3. **Environment-Specific**: May contain machine-specific paths
4. **Build Reproducibility**: Each developer should build from source

Our `.gitignore` includes:
```gitignore
# Maven
target/
```

## Viewing Test Reports

### Option 1: Command Line (Quick)
```bash
# View text summary
cat target/surefire-reports/com.ecse429.restapi.CategoriesApiExpectedBehaviorTest.txt

# View all test results
cat target/surefire-reports/*.txt | grep "Tests run"
```

### Option 2: Open in IDE
- Most IDEs (IntelliJ, Eclipse, VS Code) can parse XML reports
- Shows test results in a nice UI

### Option 3: HTML Reports (if generated)
```bash
# Generate HTML reports (requires additional plugin)
mvn surefire-report:report

# View in browser
open target/site/surefire-report.html
```

## Common Maven Commands Related to `target/`

```bash
# Clean (delete target/)
mvn clean

# Compile production code only
mvn compile                 # → target/classes/

# Compile test code only
mvn test-compile           # → target/test-classes/

# Run tests
mvn test                   # → target/surefire-reports/

# Clean and test
mvn clean test             # Fresh build + tests

# Package (create JAR)
mvn package                # → target/*.jar
```

## Practical Examples

### Example 1: Finding Test Failures
```bash
# Search for failures in reports
grep -r "Failures:" target/surefire-reports/*.txt
```

### Example 2: Checking Test Execution Time
```bash
# Find slowest tests
grep "Time elapsed" target/surefire-reports/*.txt | sort -k4 -n
```

### Example 3: Getting Test Statistics
```bash
# Count total tests
grep "Tests run:" target/surefire-reports/*.txt | \
  awk '{sum+=$3; fail+=$5; err+=$7} END {print "Total:", sum, "Failed:", fail, "Errors:", err}'
```

## What Happens When You Run Tests?

### Step-by-Step Process

1. **Maven Surefire Plugin Activates**
   ```
   [INFO] --- maven-surefire-plugin:3.1.2:test ---
   ```

2. **Finds Compiled Test Classes**
   - Scans `target/test-classes/`
   - Looks for `*Test.java` or `*Tests.java`

3. **Executes Tests**
   - JUnit 5 runs each `@Test` method
   - RestAssured makes HTTP calls
   - Assertions are validated

4. **Generates Reports**
   - Creates `.txt` files for human reading
   - Creates `.xml` files for tools/CI/CD
   - Prints summary to console

5. **Returns Exit Code**
   - `0` = All tests passed
   - `1` = Some tests failed

## Size Considerations

The `target/` folder can grow large:

```bash
# Check size
du -sh target/

# Typical sizes:
# - Small projects: 1-10 MB
# - Medium projects: 10-100 MB  
# - Large projects: 100+ MB
```

**Best Practice**: Run `mvn clean` regularly to save disk space.

## Troubleshooting

### Problem: "target/ doesn't exist"
**Solution**: Run `mvn compile` or `mvn test-compile` first

### Problem: "Old test results showing"
**Solution**: Run `mvn clean test` for fresh results

### Problem: "target/ is huge"
**Solution**: Run `mvn clean` to delete it

### Problem: "Can't find test reports"
**Solution**: Check `target/surefire-reports/` after running `mvn test`

## Summary

| Folder | Purpose | When Created |
|--------|---------|--------------|
| `target/classes/` | Compiled production code | `mvn compile` |
| `target/test-classes/` | Compiled test code | `mvn test-compile` |
| `target/surefire-reports/` | Test results | `mvn test` |
| `target/maven-status/` | Build metadata | Any Maven command |
| `target/*.jar` | Packaged artifact | `mvn package` |

## Key Takeaways

✅ **`target/` is temporary** - Can be deleted and regenerated anytime  
✅ **Never commit to Git** - Always in `.gitignore`  
✅ **Test reports go here** - Check `surefire-reports/` for results  
✅ **Clean regularly** - Use `mvn clean` to free disk space  
✅ **Maven convention** - All Java/Maven projects use this structure  

---

**TL;DR**: The `target/` folder stores compiled code and test results. It's automatically created by Maven and should never be committed to Git. The most important subfolder for you is `target/surefire-reports/`, which contains your test execution results!
