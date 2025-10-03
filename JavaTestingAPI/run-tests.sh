#!/bin/bash

# ECSE429 REST API Test Runner Script
# This script helps run tests easily

echo "==================================="
echo "ECSE429 REST API Test Runner"
echo "==================================="
echo ""

# Check if API server is running
echo "Checking if API server is running on http://localhost:4567..."
if curl -s http://localhost:4567/docs > /dev/null 2>&1; then
    echo "✓ API server is running"
else
    echo "✗ API server is NOT running!"
    echo ""
    echo "Please start the server first:"
    echo "  java -jar runTodoManagerRestAPI-1.5.5.jar"
    echo ""
    exit 1
fi

echo ""
echo "==================================="

# Parse command line arguments
if [ $# -eq 0 ]; then
    echo "Running ALL tests..."
    mvn test
elif [ "$1" == "categories" ]; then
    echo "Running Categories tests..."
    mvn test -Dtest=CategoriesApi*Test
elif [ "$1" == "todos" ]; then
    echo "Running Todos tests..."
    mvn test -Dtest=TodosApi*Test
elif [ "$1" == "projects" ]; then
    echo "Running Projects tests..."
    mvn test -Dtest=ProjectsApi*Test
elif [ "$1" == "expected" ]; then
    echo "Running Expected Behavior tests..."
    mvn test -Dtest=*ExpectedBehaviorTest
elif [ "$1" == "actual" ]; then
    echo "Running Actual Behavior tests..."
    mvn test -Dtest=*ActualBehaviorTest
elif [ "$1" == "clean" ]; then
    echo "Cleaning build artifacts..."
    mvn clean
else
    echo "Running test class: $1"
    mvn test -Dtest=$1
fi

echo ""
echo "==================================="
echo "Test run complete!"
echo "View detailed reports in: target/surefire-reports/"
echo "==================================="
