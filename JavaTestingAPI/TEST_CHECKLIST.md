# Test Execution Checklist

Use this checklist to ensure proper test execution and validation.

## Pre-Execution Checklist

- [ ] Java 11+ installed: `java -version`
- [ ] Maven 3.6+ installed: `mvn -version`
- [ ] Project dependencies downloaded: `mvn dependency:resolve`
- [ ] API server JAR file available: `runTodoManagerRestAPI-1.5.5.jar`
- [ ] Port 4567 is available: `lsof -i :4567`

## Server Startup Checklist

- [ ] Navigate to project directory
- [ ] Start server: `java -jar runTodoManagerRestAPI-1.5.5.jar`
- [ ] Verify server responds: `curl http://localhost:4567/docs`
- [ ] Check server logs for errors
- [ ] Keep server terminal window open

## Test Execution Checklist

### Initial Build
- [ ] Navigate to JavaTestingAPI directory
- [ ] Clean previous builds: `mvn clean`
- [ ] Compile tests: `mvn test-compile`
- [ ] Verify no compilation errors

### Run Categories Tests
- [ ] Expected behavior: `mvn test -Dtest=CategoriesApiExpectedBehaviorTest`
- [ ] Actual behavior: `mvn test -Dtest=CategoriesApiActualBehaviorTest`
- [ ] All tests pass (or document failures)
- [ ] Review test output for any warnings

### Run Todos Tests
- [ ] Expected behavior: `mvn test -Dtest=TodosApiExpectedBehaviorTest`
- [ ] Actual behavior: `mvn test -Dtest=TodosApiActualBehaviorTest`
- [ ] All tests pass (or document failures)
- [ ] Review test output for any warnings

### Run Projects Tests
- [ ] Expected behavior: `mvn test -Dtest=ProjectsApiExpectedBehaviorTest`
- [ ] Actual behavior: `mvn test -Dtest=ProjectsApiActualBehaviorTest`
- [ ] All tests pass (or document failures)
- [ ] Review test output for any warnings

### Run All Tests
- [ ] Execute: `mvn test` or `./run-tests.sh`
- [ ] Check total test count (should be ~107)
- [ ] Verify pass rate
- [ ] Review any failures

## Post-Execution Checklist

### Review Results
- [ ] Check console output for summary
- [ ] Open HTML reports: `target/surefire-reports/*.html`
- [ ] Review text reports: `target/surefire-reports/*.txt`
- [ ] Check for any flaky tests (re-run if needed)

### Validate Coverage
- [ ] All CRUD operations tested (Create, Read, Update, Delete)
- [ ] Error cases tested (404s, 400s)
- [ ] Edge cases tested (empty body, malformed JSON)
- [ ] Both JSON and XML formats tested

### Documentation
- [ ] Document any test failures
- [ ] Note differences between expected and actual behavior
- [ ] Record any bugs discovered
- [ ] Update test cases if needed

## Common Test Scenarios

### Scenario 1: First Time Setup
```bash
cd JavaTestingAPI
mvn clean install
./run-tests.sh
```

### Scenario 2: Quick Test Run
```bash
./run-tests.sh categories  # Just categories
./run-tests.sh             # All tests
```

### Scenario 3: Debugging Single Test
```bash
mvn test -Dtest=CategoriesApiExpectedBehaviorTest#testGetAllCategories
```

### Scenario 4: Full Validation
```bash
mvn clean test
# Review all reports
open target/surefire-reports/index.html
```

## Troubleshooting Checklist

### Server Issues
- [ ] Server not starting
  - Check if port 4567 is already in use
  - Verify JAR file exists and is not corrupted
  - Check Java version compatibility
  
- [ ] Server crashes during tests
  - Check server logs for stack traces
  - Verify sufficient memory available
  - Check for resource leaks

### Test Issues
- [ ] Tests won't compile
  - Run `mvn clean`
  - Verify pom.xml is valid
  - Check Java version matches project requirements
  
- [ ] Tests failing
  - Verify server is running
  - Check if server state is clean (restart server)
  - Verify network connectivity to localhost
  - Check for port conflicts
  
- [ ] Intermittent failures
  - Increase timeout values if needed
  - Check for race conditions
  - Verify cleanup in @AfterEach methods
  - Run tests individually to isolate issues

### Maven Issues
- [ ] Dependencies not downloading
  - Check internet connection
  - Clear Maven cache: `rm -rf ~/.m2/repository`
  - Re-run: `mvn dependency:resolve`
  
- [ ] Build failures
  - Check Maven version: `mvn -version`
  - Verify JAVA_HOME is set correctly
  - Try: `mvn clean install -U`

## Test Metrics to Track

### Execution Metrics
- [ ] Total tests run: _______
- [ ] Tests passed: _______
- [ ] Tests failed: _______
- [ ] Tests skipped: _______
- [ ] Execution time: _______

### Coverage Metrics
- [ ] Endpoints tested: 3 (categories, todos, projects)
- [ ] HTTP methods tested: 5 (GET, HEAD, POST, PUT, DELETE)
- [ ] Status codes validated: 4+ (200, 201, 400, 404)
- [ ] Edge cases covered: 20+

### Quality Metrics
- [ ] All tests have assertions
- [ ] All tests are idempotent (have cleanup)
- [ ] All tests have meaningful names
- [ ] No hard-coded values (except test data)

## Sign-Off

### Test Execution Completed By
- Name: _________________
- Date: _________________
- Environment: _________________

### Results Summary
- Total Tests: _______
- Pass Rate: _______% 
- Notable Findings: _________________
- Action Items: _________________

### Approvals
- Tests Reviewed By: _________________
- Date: _________________
- Status: [ ] Approved  [ ] Needs Revision

---

## Quick Reference Commands

```bash
# Start server
java -jar runTodoManagerRestAPI-1.5.5.jar

# Run all tests
./run-tests.sh

# Run specific resource tests
./run-tests.sh categories
./run-tests.sh todos
./run-tests.sh projects

# Run by behavior type
./run-tests.sh expected
./run-tests.sh actual

# Maven commands
mvn clean                    # Clean build
mvn test                     # Run all tests
mvn test -Dtest=ClassName   # Run specific class
mvn clean test              # Clean and test

# View reports
open target/surefire-reports/index.html
```

## Notes

_Use this space for any additional notes or observations:_

_______________________________________________
_______________________________________________
_______________________________________________
_______________________________________________
_______________________________________________
