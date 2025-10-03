# ECSE429 REST API Testing with JUnit 5 and RestAssured

This project contains comprehensive JUnit 5 test suites for the Todo Manager REST API using RestAssured.

## Project Structure

```
JavaTestingAPI/
├── pom.xml                          # Maven configuration with dependencies
├── src/
│   └── test/
│       └── java/
│           └── com/ecse429/restapi/
│               ├── BaseApiTest.java                        # Base class with RestAssured setup
│               ├── CategoriesApiExpectedBehaviorTest.java  # Categories expected behavior tests
│               ├── CategoriesApiActualBehaviorTest.java    # Categories actual behavior tests
│               ├── TodosApiExpectedBehaviorTest.java       # Todos expected behavior tests
│               ├── TodosApiActualBehaviorTest.java         # Todos actual behavior tests
│               ├── ProjectsApiExpectedBehaviorTest.java    # Projects expected behavior tests
│               └── ProjectsApiActualBehaviorTest.java      # Projects actual behavior tests
└── README.md
```

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- The REST API server running at `http://localhost:4567`

## Dependencies

The project uses:
- **JUnit 5 (Jupiter)** - Version 5.10.0 - Testing framework
- **RestAssured** - Version 5.3.2 - HTTP client for testing REST APIs
- **Hamcrest** - Version 2.2 - Matchers for assertions
- **Gson** - Version 2.10.1 - JSON processing

## Starting the API Server

Before running the tests, start the Todo Manager REST API server:

```bash
# From the project root directory
java -jar runTodoManagerRestAPI-1.5.5.jar
```

The server should start on `http://localhost:4567`. You can verify it's running by visiting:
```bash
curl http://localhost:4567/docs
```

## Running the Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
# Run Categories tests
mvn test -Dtest=CategoriesApiExpectedBehaviorTest
mvn test -Dtest=CategoriesApiActualBehaviorTest

# Run Todos tests
mvn test -Dtest=TodosApiExpectedBehaviorTest
mvn test -Dtest=TodosApiActualBehaviorTest

# Run Projects tests
mvn test -Dtest=ProjectsApiExpectedBehaviorTest
mvn test -Dtest=ProjectsApiActualBehaviorTest
```

### Run Specific Test Method

```bash
mvn test -Dtest=CategoriesApiExpectedBehaviorTest#testGetAllCategories
```

### Run with Verbose Output

```bash
mvn test -X
```

## Test Organization

### Expected Behavior Tests
These test classes verify the API according to its documentation:
- `CategoriesApiExpectedBehaviorTest`
- `TodosApiExpectedBehaviorTest`
- `ProjectsApiExpectedBehaviorTest`

### Actual Behavior Tests
These test classes document the actual API behavior, which may differ from documentation:
- `CategoriesApiActualBehaviorTest`
- `TodosApiActualBehaviorTest`
- `ProjectsApiActualBehaviorTest`

## Test Coverage

### Categories API Tests (`/categories`)
- ✅ GET /categories - List all categories
- ✅ HEAD /categories - Check endpoint availability
- ✅ POST /categories - Create new category
- ✅ GET /categories/{id} - Get specific category
- ✅ POST /categories/{id} - Update category
- ✅ PUT /categories/{id} - Replace category
- ✅ DELETE /categories/{id} - Delete category
- ✅ Error handling for non-existent resources
- ✅ XML format support
- ✅ Edge cases (empty body, malformed JSON, etc.)

### Todos API Tests (`/todos`)
- ✅ GET /todos - List all todos
- ✅ HEAD /todos - Check endpoint availability
- ✅ POST /todos - Create new todo
- ✅ GET /todos/{id} - Get specific todo
- ✅ POST /todos/{id} - Update todo
- ✅ PUT /todos/{id} - Replace todo
- ✅ DELETE /todos/{id} - Delete todo
- ✅ doneStatus field handling
- ✅ Error handling for non-existent resources
- ✅ XML format support
- ✅ Edge cases

### Projects API Tests (`/projects`)
- ✅ GET /projects - List all projects
- ✅ HEAD /projects - Check endpoint availability
- ✅ POST /projects - Create new project
- ✅ GET /projects/{id} - Get specific project
- ✅ POST /projects/{id} - Update project
- ✅ PUT /projects/{id} - Replace project
- ✅ DELETE /projects/{id} - Delete project
- ✅ completed and active field handling
- ✅ Error handling for non-existent resources
- ✅ XML format support
- ✅ Edge cases

## Key Features

### Idempotent Tests
All tests clean up after themselves using `@AfterEach` methods to delete created resources.

### Clean Code Principles
- Small, focused test methods
- Meaningful test names with `@DisplayName`
- Clear test organization with sections
- Proper use of assertions
- No code duplication

### Comprehensive Assertions
- Status code validation
- Response body field validation
- Error message validation
- Content type validation
- Data integrity checks

### Test Ordering
Tests use `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` with `@Order` annotations for predictable execution.

## Troubleshooting

### Tests Failing with Connection Refused
Make sure the API server is running:
```bash
java -jar runTodoManagerRestAPI-1.5.5.jar
```

### Tests Failing with 404 Errors
The API might have been restarted and resource IDs changed. Tests are designed to be idempotent and create their own test data.

### Port Already in Use
If port 4567 is already in use, you'll need to:
1. Stop the process using the port
2. Or modify `BaseApiTest.java` to use a different port

### Maven Build Errors
Ensure you have the correct Java version:
```bash
java -version  # Should be Java 11 or higher
mvn -version   # Should be Maven 3.6 or higher
```

## Viewing Test Results

After running tests, view the results in:
- Console output
- `target/surefire-reports/` - HTML and XML reports
- `target/surefire-reports/*.txt` - Text summaries

## Example Test Output

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.ecse429.restapi.CategoriesApiExpectedBehaviorTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.ecse429.restapi.TodosApiExpectedBehaviorTest
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.ecse429.restapi.ProjectsApiExpectedBehaviorTest
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 52, Failures: 0, Errors: 0, Skipped: 0
```

## Contributing

When adding new tests:
1. Extend `BaseApiTest` class
2. Follow the existing naming conventions
3. Add cleanup in `@AfterEach` method
4. Use meaningful `@DisplayName` annotations
5. Group related tests with comments
6. Add appropriate assertions

## License

This project is part of ECSE429 coursework.
