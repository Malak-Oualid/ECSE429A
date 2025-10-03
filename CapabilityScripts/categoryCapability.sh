#!/bin/bash

BASE_URL="http://localhost:4567/categories"
CONTENT_TYPE="Content-Type: application/json"

echo "==== API TESTING START ===="

echo "[6:00 PM] GET /categories"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE_URL"
echo

echo "[6:05 PM] HEAD /categories"
curl -s -I -X HEAD "$BASE_URL"
echo

echo "[6:09 PM] POST /categories (title: 67)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE_URL" \
     -H "$CONTENT_TYPE" \
     -d '{"title":"67"}'
echo

echo "[6:14 PM] GET /categories/5 (nonexistent)"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE_URL/5"
echo

echo "[6:18 PM] GET /categories/3"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE_URL/3"
echo

echo "[6:22 PM] HEAD /categories/3"
curl -s -I -X HEAD "$BASE_URL/3"
echo

echo "[6:28 PM] POST /categories/3 (description: 67description)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE_URL/3" \
     -H "$CONTENT_TYPE" \
     -d '{"description":"67description"}'
echo

echo "[6:34 PM] POST /categories/6 (invalid)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE_URL/6" \
     -H "$CONTENT_TYPE" \
     -d '{"description":"invalid"}'
echo

echo "[6:39 PM] PUT /categories/3 (description: put67description)"
curl -s -w "\nHTTP %{http_code}\n" -X PUT "$BASE_URL/3" \
     -H "$CONTENT_TYPE" \
     -d '{"description":"put67description"}'
echo

echo "[6:45 PM] PUT /categories/6 (invalid)"
curl -s -w "\nHTTP %{http_code}\n" -X PUT "$BASE_URL/6" \
     -H "$CONTENT_TYPE" \
     -d '{"description":"invalid"}'
echo

echo "[6:51 PM] DELETE /categories/3"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE_URL/3"
echo

echo "[6:55 PM] DELETE /categories/6"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE_URL/6"
echo

echo "==== API TESTING END ===="
