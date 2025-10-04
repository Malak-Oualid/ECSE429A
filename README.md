# ECSE429 REST API Testing with JUnit 5 and RestAssured

This project contains comprehensive JUnit 5 test suites for the Todo Manager REST API using RestAssured.

## Project Structure

```
JavaTestingAPI/
├── pom.xml                     # Maven configuration with dependencies
├── src/test/java/com/ecse429/restapi/
│   ├── BaseApiTest.java        # Base class with RestAssured setup
│   ├── JsonTests/
|   |   └── todos/
|   |       ├── TodosApiJsonTest.java
|   |   └── projects/
|   |       ├── ProjectsApiJsonTest.java
│   │   └── categories/
│   │       ├── CategoriesApiJsonTest.java        
│   │   └── interoperability/
│   │       ├── InteroperabilityApiJsonTest.java
│   └── XmlTests/
|   |   └── todos/
|   |       ├── TodosApiXmlTest.java
|   |   └── projects/
|   |       ├── ProjectsApiXmlTest.java
│   │   └── categories/
│   │       ├── CategoriesApiXmlTest.java        
│   │   └── interoperability/
│   │       ├── InteroperabilityApiXmlTest.java
└── README.md
```

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- The REST API server running at `http://localhost:4567`

## Quick Start

### 1. Start the API Server
Before running tests, ensure the Todo Manager REST API server is running:
```bash
# The server should be running on http://localhost:4567
# Verify with: curl http://localhost:4567/categories
```

### 2. Run All Tests
```bash
cd JavaTestingAPI
mvn test
```

### 3. Run Specific Test Categories

**JSON Tests:**
```bash
# Run basic categories CRUD tests
mvn test -Dtest=CategoriesApiTest

# Run comprehensive behavior tests
mvn test -Dtest=CategoriesApiExpectedBehaviorTest
```

**XML Tests:**
```bash
# Run XML format tests
mvn test -Dtest=CategoriesApiXmlTest
```

### 4. Run Individual Test Methods
```bash
# Run a specific test method
mvn test -Dtest=CategoriesApiTest#testCreateCategory

# Run multiple specific methods
mvn test -Dtest=CategoriesApiTest#testCreateCategory+testGetCategoryById
```

## Test Suites Overview

### CategoriesApiTest (JSON)
Basic CRUD operations testing:
- ✅ GET /categories - List all categories
- ✅ HEAD /categories - Check endpoint availability  
- ✅ POST /categories - Create new category
- ✅ GET /categories/{id} - Get specific category
- ✅ HEAD /categories/{id} - Check specific category
- ✅ POST /categories/{id} - Update category
- ✅ PUT /categories/{id} - Replace category  
- ✅ DELETE /categories/{id} - Delete category
- ✅ Verify 404 for deleted resources

### CategoriesApiExpectedBehaviorTest (JSON)
Comprehensive behavior and edge case testing:
- ✅ All CRUD operations with validation
- ✅ Error handling scenarios
- ✅ Malformed requests
- ✅ Data integrity checks
- ✅ Boundary conditions

### CategoriesApiXmlTest (XML)
XML format testing:
- ✅ All CRUD operations using XML payloads
- ✅ XML response validation
- ✅ Content-Type handling for XML

## Dependencies

The project uses:
- **JUnit 5 (Jupiter)** - Version 5.10.0 - Testing framework
- **RestAssured** - Version 5.3.2 - HTTP client for testing REST APIs
- **Hamcrest** - Version 2.2 - Matchers for assertions
- **Gson** - Version 2.10.1 - JSON processing

## Troubleshooting

### API Server Not Running
```bash
# Check if server is running
curl http://localhost:4567/categories

# If not running, start the server first
# (Server should be provided separately)
```

### Test Failures
```bash
# Run with verbose output for debugging
mvn test -X

# Run individual test to isolate issues
mvn test -Dtest=CategoriesApiTest#testGetAllCategories
```

### Port Issues
If port 4567 is in use, modify `BaseApiTest.java`:
```java
protected static final int PORT = 4567; // Change to available port
```

### Build Issues
```bash
# Verify Java and Maven versions
java -version  # Should be Java 11+
mvn -version   # Should be Maven 3.6+

# Clean and rebuild
mvn clean compile test-compile
```

## Test Reports

After running tests, view results in:
- Console output for immediate feedback
- `target/surefire-reports/` for detailed HTML/XML reports

## Contributing

When adding new tests:
1. Extend `BaseApiTest` class for configuration
2. Follow existing naming conventions
3. Use `@TestMethodOrder` and `@Order` for test sequencing
4. Include both positive and negative test cases
5. Clean up test data when needed

## License

This project is part of ECSE429 coursework.
