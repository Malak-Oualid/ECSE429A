#!/bin/bash

BASE_URL="http://localhost:4567/projects"
CONTENT_TYPE="Content-Type: application/json"

echo "==== API TESTING START ===="

echo "[7:00 PM] GET /projects"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE_URL"
echo

echo "[7:02 PM] HEAD /projects"
curl -s -I -X HEAD "$BASE_URL"
echo

echo "[7:04 PM] POST /projects (create new project)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE_URL" \
     -H "$CONTENT_TYPE" \
     -d '{"title": "Test Project", "completed": false, "active": true, "description": "Test Description 123"}'
echo

echo "[7:07 PM] GET /projects/9999999 (nonexistent)"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE_URL/9999999"
echo

echo "[7:10 PM] GET /projects/2 (existing project)"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE_URL/2"
echo

echo "[7:13 PM] HEAD /projects/2"
curl -s -I -X HEAD "$BASE_URL/2"
echo

echo "[7:16 PM] HEAD /projects/9999999 (nonexistent)"
curl -s -I -X HEAD "$BASE_URL/9999999"
echo

echo "[7:18 PM] HEAD /projects/thisisanid (invalid id)"
curl -s -I -X HEAD "$BASE_URL/thisisanid"
echo

echo "[7:20 PM] POST /projects/2 (update title)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE_URL/2" \
     -H "$CONTENT_TYPE" \
     -d '{"title": "Updated Title"}'
echo

echo "[7:23 PM] POST /projects/1738 (nonexistent)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE_URL/1738" \
     -H "$CONTENT_TYPE" \
     -d '{"title": "Nonexistent Project"}'
echo

echo "[7:26 PM] PUT /projects/2 (overwrite all fields)"
curl -s -w "\nHTTP %{http_code}\n" -X PUT "$BASE_URL/2" \
     -H "$CONTENT_TYPE" \
     -d '{"title": "Overwritten Project", "completed": true, "active": false, "description": "Overwrite test"}'
echo

echo "[7:29 PM] PUT /projects/3774 (nonexistent)"
curl -s -w "\nHTTP %{http_code}\n" -X PUT "$BASE_URL/3774" \
     -H "$CONTENT_TYPE" \
     -d '{"title": "Nonexistent PUT"}'
echo

echo "[7:32 PM] DELETE /projects/3"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE_URL/2"
echo

echo "[7:35 PM] DELETE /projects/28 (nonexistent)"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE_URL/28"
echo

echo "[7:38 PM] GET /projects/2 (after deletion)"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE_URL/2"
echo

echo "[7:40 PM] DELETE /projects/2 (after prior deletion)"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE_URL/2"
echo

echo "[7:43 PM] POST /projects (partial fields)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE_URL" \
     -H "$CONTENT_TYPE" \
     -d '{"title": "Partial Project???"}'
echo

echo "[7:45 PM] POST /projects (missing all fields)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE_URL" \
     -H "$CONTENT_TYPE" \
     -d '{}'
echo

echo "==== API TESTING END ===="