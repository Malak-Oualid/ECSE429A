#!/bin/bash

BASE="http://localhost:4567"
CT_JSON="Content-Type: application/json"

echo "==== API TESTING START (Interoperability JSON) ===="

# --- Create entities ---
echo "[6:00 PM] POST /todos (create todo)"
todoResp=$(curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE/todos" \
  -H "$CT_JSON" -H "Accept: application/json" \
  -d '{"title":"interopTodo","doneStatus":false,"description":"interop todo"}')
echo "$todoResp"
todoId=$(sed '$d' <<< "$todoResp" | jq -r '.id // .todo.id // .todos[0].id')

echo "[6:05 PM] POST /projects (create project)"
projResp=$(curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE/projects" \
  -H "$CT_JSON" -H "Accept: application/json" \
  -d '{"title":"interopProject","completed":false,"active":true,"description":"interop project"}')
echo "$projResp"
projectId=$(sed '$d' <<< "$projResp" | jq -r '.id // .project.id // .projects[0].id')

echo "[6:09 PM] POST /categories (create category)"
catResp=$(curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE/categories" \
  -H "$CT_JSON" -H "Accept: application/json" \
  -d '{"title":"interopCategory","description":"interop category"}')
echo "$catResp"
categoryId=$(sed '$d' <<< "$catResp" | jq -r '.id // .category.id // .categories[0].id')

echo
echo "# ids => todoId=$todoId projectId=$projectId categoryId=$categoryId"
echo

# --- Link Todo -> Project (via project tasks) ---
echo "[6:14 PM] POST /projects/$projectId/tasks (link todo)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE/projects/$projectId/tasks" \
  -H "$CT_JSON" -H "Accept: application/json" \
  -d "{\"id\":\"$todoId\"}"
echo

echo "[6:18 PM] GET /projects/$projectId/tasks (verify todo present)"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE/projects/$projectId/tasks" -H "Accept: application/json"
echo

echo "[6:22 PM] GET /todos/$todoId/tasksof (verify project present)"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE/todos/$todoId/tasksof" -H "Accept: application/json"
echo

# --- Link from Todo side -> Project (tasksof) ---
echo "[6:26 PM] POST /todos/$todoId/tasksof (link project)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE/todos/$todoId/tasksof" \
  -H "$CT_JSON" -H "Accept: application/json" \
  -d "{\"id\":\"$projectId\"}"
echo

echo "[6:30 PM] GET /todos/$todoId/tasksof (verify)"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE/todos/$todoId/tasksof" -H "Accept: application/json"
echo

echo "[6:34 PM] GET /projects/$projectId/tasks (verify)"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE/projects/$projectId/tasks" -H "Accept: application/json"
echo

# --- Link Todo <-> Category ---
echo "[6:38 PM] POST /todos/$todoId/categories (link category)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE/todos/$todoId/categories" \
  -H "$CT_JSON" -H "Accept: application/json" \
  -d "{\"id\":\"$categoryId\"}"
echo

echo "[6:42 PM] GET /todos/$todoId/categories (verify)"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE/todos/$todoId/categories" -H "Accept: application/json"
echo

# --- Link Project <-> Category ---
echo "[6:46 PM] POST /projects/$projectId/categories (link category)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE/projects/$projectId/categories" \
  -H "$CT_JSON" -H "Accept: application/json" \
  -d "{\"id\":\"$categoryId\"}"
echo

echo "[6:50 PM] GET /projects/$projectId/categories (verify)"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE/projects/$projectId/categories" -H "Accept: application/json"
echo

# --- Category-side mirrors ---
echo "[6:54 PM] POST /categories/$categoryId/todos (mirror link todo)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE/categories/$categoryId/todos" \
  -H "$CT_JSON" -H "Accept: application/json" \
  -d "{\"id\":\"$todoId\"}"
echo

echo "[6:58 PM] POST /categories/$categoryId/projects (mirror link project)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE/categories/$categoryId/projects" \
  -H "$CT_JSON" -H "Accept: application/json" \
  -d "{\"id\":\"$projectId\"}"
echo

echo "[7:02 PM] GET /categories/$categoryId/todos (verify)"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE/categories/$categoryId/todos" -H "Accept: application/json"
echo

echo "[7:06 PM] GET /categories/$categoryId/projects (verify)"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE/categories/$categoryId/projects" -H "Accept: application/json"
echo

# --- HEAD smoke checks (collections, instances, relationships) ---
echo "[7:10 PM] HEAD /todos"
curl -s -I -X HEAD "$BASE/todos"
echo

echo "[7:12 PM] HEAD /projects"
curl -s -I -X HEAD "$BASE/projects"
echo

echo "[7:14 PM] HEAD /categories"
curl -s -I -X HEAD "$BASE/categories"
echo

echo "[7:16 PM] HEAD /todos/$todoId"
curl -s -I -X HEAD "$BASE/todos/$todoId"
echo

echo "[7:18 PM] HEAD /projects/$projectId"
curl -s -I -X HEAD "$BASE/projects/$projectId"
echo

echo "[7:20 PM] HEAD /categories/$categoryId"
curl -s -I -X HEAD "$BASE/categories/$categoryId"
echo

echo "[7:22 PM] HEAD /projects/$projectId/tasks"
curl -s -I -X HEAD "$BASE/projects/$projectId/tasks"
echo

echo "[7:24 PM] HEAD /todos/$todoId/tasksof"
curl -s -I -X HEAD "$BASE/todos/$todoId/tasksof"
echo

echo "[7:26 PM] HEAD /todos/$todoId/categories"
curl -s -I -X HEAD "$BASE/todos/$todoId/categories"
echo

echo "[7:28 PM] HEAD /projects/$projectId/categories"
curl -s -I -X HEAD "$BASE/projects/$projectId/categories"
echo

echo "[7:30 PM] HEAD /categories/$categoryId/todos"
curl -s -I -X HEAD "$BASE/categories/$categoryId/todos"
echo

echo "[7:32 PM] HEAD /categories/$categoryId/projects"
curl -s -I -X HEAD "$BASE/categories/$categoryId/projects"
echo

# --- Filters (light) + Amend + Cleanup of the extra filtered todo ---
echo "[7:36 PM] POST /todos (create done todo for filter)"
doneResp=$(curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE/todos" \
  -H "$CT_JSON" -H "Accept: application/json" \
  -d '{"title":"doneOne","doneStatus":true}')
echo "$doneResp"
doneTodoId=$(sed '$d' <<< "$doneResp" | jq -r '.id // .todo.id // .todos[0].id')

echo "[7:40 PM] GET /todos?doneStatus=true (verify filter includes created)"
curl -s -w "\nHTTP %{http_code}\n" -X GET "$BASE/todos?doneStatus=true" -H "Accept: application/json"
echo

echo "[7:44 PM] DELETE /todos/$doneTodoId (cleanup filtered)"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE/todos/$doneTodoId"
echo

# --- Amend endpoints (PUT/POST) ---
echo "[7:48 PM] PUT /todos/$todoId (title, doneStatus=true)"
curl -s -w "\nHTTP %{http_code}\n" -X PUT "$BASE/todos/$todoId" \
  -H "$CT_JSON" -H "Accept: application/json" \
  -d '{"title":"interopTodoUpdated","doneStatus":true}'
echo

echo "[7:52 PM] POST /projects/$projectId (description patch)"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE/projects/$projectId" \
  -H "$CT_JSON" -H "Accept: application/json" \
  -d '{"description":"interop project patched"}'
echo

echo "[7:56 PM] PUT /categories/$categoryId (description update)"
curl -s -w "\nHTTP %{http_code}\n" -X PUT "$BASE/categories/$categoryId" \
  -H "$CT_JSON" -H "Accept: application/json" \
  -d '{"description":"interop category updated"}'
echo

# --- Unlink + Delete entities (best-effort) ---
echo "[8:00 PM] DELETE /projects/$projectId/tasks/$todoId (unlink)"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE/projects/$projectId/tasks/$todoId"
echo

echo "[8:02 PM] DELETE /todos/$todoId/tasksof/$projectId (unlink mirror)"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE/todos/$todoId/tasksof/$projectId"
echo

echo "[8:04 PM] DELETE /todos/$todoId/categories/$categoryId (unlink)"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE/todos/$todoId/categories/$categoryId"
echo

echo "[8:06 PM] DELETE /projects/$projectId/categories/$categoryId (unlink)"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE/projects/$projectId/categories/$categoryId"
echo

echo "[8:08 PM] DELETE /categories/$categoryId/todos/$todoId (unlink mirror)"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE/categories/$categoryId/todos/$todoId"
echo

echo "[8:10 PM] DELETE /categories/$categoryId/projects/$projectId (unlink mirror)"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE/categories/$categoryId/projects/$projectId"
echo

echo "[8:12 PM] DELETE /todos/$todoId"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE/todos/$todoId"
echo

echo "[8:14 PM] DELETE /projects/$projectId"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE/projects/$projectId"
echo

echo "[8:16 PM] DELETE /categories/$categoryId"
curl -s -w "\nHTTP %{http_code}\n" -X DELETE "$BASE/categories/$categoryId"
echo

echo "==== API TESTING END ===="