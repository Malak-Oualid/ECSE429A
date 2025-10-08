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

## Contribution
| Jason Shao | Malak Oualid | Natasha Lawford | Joseph Feghaly |
|-----------|-----------|-----------|-----------|
| Category Unit test and script | Todo Unit test and script | Project Unit test and script | Interoperability Unit test and script |

## License

This project is part of ECSE429 coursework.
