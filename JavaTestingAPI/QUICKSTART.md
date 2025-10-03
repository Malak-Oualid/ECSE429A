# Quick Start Guide

## 1. Start the API Server

```bash
# From the main project directory
cd /Users/jason/Desktop/McGill/U3/ECSE429/Project
java -jar runTodoManagerRestAPI-1.5.5.jar
```

Keep this terminal window open. The server will be running at `http://localhost:4567`.

## 2. Run the Tests

Open a new terminal window and navigate to the test project:

```bash
cd /Users/jason/Desktop/McGill/U3/ECSE429/Project/JavaTestingAPI
```

### Option A: Use the Test Runner Script (Recommended)

```bash
# Run all tests
./run-tests.sh

# Run specific resource tests
./run-tests.sh categories
./run-tests.sh todos
./run-tests.sh projects

# Run by behavior type
./run-tests.sh expected
./run-tests.sh actual

# Run specific test class
./run-tests.sh CategoriesApiExpectedBehaviorTest

# Clean build artifacts
./run-tests.sh clean
```

### Option B: Use Maven Directly

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CategoriesApiExpectedBehaviorTest

# Run specific test method
mvn test -Dtest=CategoriesApiExpectedBehaviorTest#testGetAllCategories

# Clean and test
mvn clean test
```

## 3. View Test Results

- **Console Output**: Real-time test results in your terminal
- **HTML Reports**: `target/surefire-reports/*.html`
- **Text Summaries**: `target/surefire-reports/*.txt`

## Test Summary

| Test Class | Tests | Endpoint |
|------------|-------|----------|
| CategoriesApiExpectedBehaviorTest | 15 | /categories |
| CategoriesApiActualBehaviorTest | 17 | /categories |
| TodosApiExpectedBehaviorTest | 17 | /todos |
| TodosApiActualBehaviorTest | 18 | /todos |
| ProjectsApiExpectedBehaviorTest | 20 | /projects |
| ProjectsApiActualBehaviorTest | 20 | /projects |
| **Total** | **107** | All endpoints |

## Common Issues

### Issue: Connection Refused
**Solution**: Make sure the API server is running on port 4567

### Issue: Tests Failing
**Solution**: Restart the API server to reset the database state

### Issue: Port Already in Use
**Solution**: Kill the process using port 4567:
```bash
lsof -ti:4567 | xargs kill -9
```

## Project Structure

```
JavaTestingAPI/
├── pom.xml                    # Maven dependencies
├── README.md                  # Full documentation
├── QUICKSTART.md             # This file
├── run-tests.sh              # Test runner script
├── .gitignore                # Git ignore rules
└── src/
    └── test/
        └── java/
            └── com/ecse429/restapi/
                ├── BaseApiTest.java
                ├── CategoriesApiExpectedBehaviorTest.java
                ├── CategoriesApiActualBehaviorTest.java
                ├── TodosApiExpectedBehaviorTest.java
                ├── TodosApiActualBehaviorTest.java
                ├── ProjectsApiExpectedBehaviorTest.java
                └── ProjectsApiActualBehaviorTest.java
```

## Example Test Run

```bash
./run-tests.sh categories
```

Expected output:
```
===================================
ECSE429 REST API Test Runner
===================================

Checking if API server is running on http://localhost:4567...
✓ API server is running

===================================
Running Categories tests...
[INFO] Scanning for projects...
[INFO] Tests run: 32, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
===================================
Test run complete!
View detailed reports in: target/surefire-reports/
===================================
```

## Next Steps

1. Review test code in `src/test/java/com/ecse429/restapi/`
2. Check the full README.md for detailed documentation
3. View test reports in `target/surefire-reports/`
4. Modify tests as needed for your requirements
