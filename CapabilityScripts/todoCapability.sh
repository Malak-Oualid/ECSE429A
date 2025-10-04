#!/bin/bash

BASE_URL="http://localhost:4567/todos"
CONTENT_TYPE="Content-Type: application/json"

echo "==== API TESTING START ===="

echo "[6:10 PM] GET /todos"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE_URL"
echo

echo "[6:15 PM] POST /todos (create new todo)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE_URL" \
     -H "$CONTENT_TYPE" \
     -d '{"title": "New Todo Item", "doneStatus": false, "description": "Exploratory test todo"}'
echo

echo "[6:18 PM] GET /todos/1 (existing todo)"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE_URL/1"
echo

echo "[6:20 PM] GET /todos/2 (existing todo)"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE_URL/2"
echo

echo "[6:22 PM] HEAD /todos"
curl -s -I -X HEAD "$BASE_URL"
echo

echo "[6:25 PM] GET /todos/78 (nonexistent todo)"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE_URL/78"
echo

echo "[6:30 PM] HEAD /todos/1 (valid id)"
curl -s -I -X HEAD "$BASE_URL/1"
echo

echo "[6:32 PM] POST /todos/1 (update title)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE_URL/1" \
     -H "$CONTENT_TYPE" \
     -d '{"title": "Updated Todo Title"}'
echo

echo "[6:35 PM] DELETE /todos/1 (valid id)"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE_URL/1"
echo

echo "[6:40 PM] DELETE /todos/78 (invalid id)"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE_URL/78"
echo

echo "[6:45 PM] PUT /todos (unsupported method)"
curl -s -w "\nHTTP %{http_code}\n" -X PUT "$BASE_URL" \
     -H "$CONTENT_TYPE" \
     -d '{"id": 5, "title": "Put Test", "doneStatus": true, "description": "Testing PUT method"}'
echo

echo "[6:55 PM] POST /todos (with id field in body)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE_URL" \
     -H "$CONTENT_TYPE" \
     -d '{"id": 999, "title": "Invalid Todo", "doneStatus": false, "description": "Should fail"}'
echo

echo "==== API TESTING END ===="
