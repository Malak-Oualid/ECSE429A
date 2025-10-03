#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:4567}"
JSON_CT_HEADER="Content-Type: application/json"

# --- helpers ---------------------------------------------------------------

need_jq() {
  if ! command -v jq >/dev/null 2>&1; then
    echo "ERROR: jq is required. Install with: brew install jq" >&2
    exit 1
  fi
}

hr() { printf '\n%s\n' "----------------------------------------------------------------------"; }

# curl wrapper that preserves both body and status code
# prints pretty JSON body (if JSON), then "HTTP <code>"
api_call() {
  local method="$1"; shift
  local url="$1"; shift
  local data="${1:-}"; shift || true

  if [[ -n "${data}" ]]; then
    # shellcheck disable=SC2086
    resp="$(curl -sS -w '\n%{http_code}' -H "$JSON_CT_HEADER" -X "$method" -d "$data" "$url")"
  else
    resp="$(curl -sS -w '\n%{http_code}' -H "$JSON_CT_HEADER" -X "$method" "$url")"
  fi

  code="$(printf "%s" "$resp" | tail -n1)"
  body="$(printf "%s" "$resp" | sed '$d')"

  if [[ -n "$body" ]]; then
    # try to pretty-print JSON, fallback to raw if not JSON
    if printf "%s" "$body" | jq . >/dev/null 2>&1; then
      printf "%s\n" "$body" | jq .
    else
      printf "%s\n" "$body"
    fi
  fi

  printf "HTTP %s\n" "$code"

  # export for callers to parse if they want
  export LAST_BODY="$body"
  export LAST_CODE="$code"
}

api_get()    { api_call GET "$1"; }
api_head()   { curl -sS -I -H "$JSON_CT_HEADER" "$1"; echo ""; }
api_delete() { api_call DELETE "$1"; }
api_post()   { api_call POST "$1" "${2:-}"; }

# Create a helper entity and return its id
create_category() {
  local title="AutoCat-$(date +%s%N)"
  api_post "${BASE_URL}/categories" "{\"title\":\"${title}\"}"
  printf "%s" "$LAST_BODY" | jq -r '..|.id? // empty' | head -n1
}

create_project() {
  local title="AutoProj-$(date +%s%N)"
  api_post "${BASE_URL}/projects" "{\"title\":\"${title}\"}"
  printf "%s" "$LAST_BODY" | jq -r '..|.id? // empty' | head -n1
}

create_todo() {
  local title="AutoTodo-$(date +%s%N)"
  api_post "${BASE_URL}/todos" "{\"title\":\"${title}\"}"
  printf "%s" "$LAST_BODY" | jq -r '..|.id? // empty' | head -n1
}

# Try to parse a relationship creation response for a single created/linked id
extract_first_id() {
  # Look for either a single object with id, or an array of items where each has id
  printf "%s" "$LAST_BODY" | jq -r '
    if type=="object" and has("id") then .id
    elif type=="object" and has("categories") then (.categories[]?.id // empty) | tostring
    elif type=="object" and has("projects") then (.projects[]?.id // empty) | tostring
    elif type=="object" and has("todos") then (.todos[]?.id // empty) | tostring
    elif type=="array" then (.[0]?.id // empty) | tostring
    else empty
    ' | head -n1
}

main() {
  need_jq
  hr; echo "Sanity: GET /todos"; hr
  api_get "${BASE_URL}/todos"

  hr; echo "GET /todos/1"; hr
  api_get "${BASE_URL}/todos/1"

  hr; echo "GET /todos/1/categories"; hr
  api_get "${BASE_URL}/todos/1/categories"

  hr; echo "GET /todos/1/categories/1 (expect 404 if not present)"; hr
  api_get "${BASE_URL}/todos/1/categories/1" || true

  # POST to /todos/1/categories without a title (expect error)
  hr; echo "POST /todos/1/categories (no body) — expect error"; hr
  api_post "${BASE_URL}/todos/1/categories" "" || true

  # POST with a title to create and relate a new category
  hr; echo "POST /todos/1/categories (with title) — expect created/linked"; hr
  api_post "${BASE_URL}/todos/1/categories" "{\"title\":\"RelCat-$(date +%s%N)\"}"
  todo_cat_id="$(extract_first_id || true)"
  if [[ -z "${todo_cat_id:-}" ]]; then
    echo "WARN: Could not parse created category id under /todos/1. Will query to discover it."
    api_get "${BASE_URL}/todos/1/categories"
    # best-effort: grab the last id listed
    todo_cat_id="$(printf "%s" "$LAST_BODY" | jq -r '.categories | last | .id // empty')"
  fi
  echo "Captured todo-related category id: ${todo_cat_id:-<unknown>}"

  # DELETE the relationship
  if [[ -n "${todo_cat_id:-}" ]]; then
    hr; echo "DELETE /todos/1/categories/${todo_cat_id}"; hr
    api_delete "${BASE_URL}/todos/1/categories/${todo_cat_id}"
  else
    echo "SKIP: no category id to delete for /todos/1"
  fi

  hr; echo "GET /todos/1/categories (verify)"; hr
  api_get "${BASE_URL}/todos/1/categories"

  hr; echo "HEAD /todos/1/categories"; hr
  api_head "${BASE_URL}/todos/1/categories"

  # Projects flow
  hr; echo "GET /projects"; hr
  api_get "${BASE_URL}/projects"

  hr; echo "GET /projects/1/categories"; hr
  api_get "${BASE_URL}/projects/1/categories"

  hr; echo "POST /projects/1/categories (with title)"; hr
  api_post "${BASE_URL}/projects/1/categories" "{\"title\":\"ProjRelCat-$(date +%s%N)\"}"
  proj_cat_id="$(extract_first_id || true)"
  echo "Captured project-related category id: ${proj_cat_id:-<unknown>}"

  if [[ -n "${proj_cat_id:-}" ]]; then
    hr; echo "DELETE /projects/1/categories/${proj_cat_id}"; hr
    api_delete "${BASE_URL}/projects/1/categories/${proj_cat_id}" || true

    hr; echo "HEAD /projects/1/categories/${proj_cat_id}"; hr
    api_head "${BASE_URL}/projects/1/categories/${proj_cat_id}" || true
  else
    echo "SKIP: no category id to delete/head for /projects/1"
  fi

  hr; echo "GET /projects/1/categories (verify empty or reduced)"; hr
  api_get "${BASE_URL}/projects/1/categories"

  # Categories collection
  hr; echo "GET /categories"; hr
  api_get "${BASE_URL}/categories"

  # Build a working category, project, todo for relationship tests
  hr; echo "Create working entities for relationship tests"; hr
  CAT_ID="$(create_category)"
  PROJ_ID="$(create_project)"
  TODO_ID="$(create_todo)"
  echo "Working IDs -> category:${CAT_ID} project:${PROJ_ID} todo:${TODO_ID}"

  # POST /categories/{id}/projects (link an existing project)
  hr; echo "POST /categories/${CAT_ID}/projects (link existing project)"; hr
  api_post "${BASE_URL}/categories/${CAT_ID}/projects" "{\"id\":\"${PROJ_ID}\"}"

  # DELETE /categories/{id}/projects/{id}
  hr; echo "DELETE /categories/${CAT_ID}/projects/${PROJ_ID}"; hr
  api_delete "${BASE_URL}/categories/${CAT_ID}/projects/${PROJ_ID}" || true

  # GET /categories/{id}/todos (should be empty initially)
  hr; echo "GET /categories/${CAT_ID}/todos"; hr
  api_get "${BASE_URL}/categories/${CAT_ID}/todos"

  # POST /categories/{id}/todos (create & link new todo via title)
  hr; echo "POST /categories/${CAT_ID}/todos (create new via title)"; hr
  api_post "${BASE_URL}/categories/${CAT_ID}/todos" "{\"title\":\"RelTodo-$(date +%s%N)\"}"
  rel_todo_id="$(extract_first_id || true)"
  if [[ -z "${rel_todo_id:-}" ]]; then
    echo "WARN: Could not parse created todo id under category. Listing to find one…"
    api_get "${BASE_URL}/categories/${CAT_ID}/todos"
    rel_todo_id="$(printf "%s" "$LAST_BODY" | jq -r '.todos | last | .id // empty')"
  fi
  echo "Captured related todo id: ${rel_todo_id:-<unknown>}"

  # DELETE /categories/{id}/todos/{id}
  if [[ -n "${rel_todo_id:-}" ]]; then
    hr; echo "DELETE /categories/${CAT_ID}/todos/${rel_todo_id}"; hr
    api_delete "${BASE_URL}/categories/${CAT_ID}/todos/${rel_todo_id}" || true
  else
    echo "SKIP: no related todo id to delete"
  fi

  # GET /categories/{id}/todos (verify empty or reduced)
  hr; echo "GET /categories/${CAT_ID}/todos (verify)"; hr
  api_get "${BASE_URL}/categories/${CAT_ID}/todos"

  hr; echo "Session complete. All category-related exploratory tests executed."; hr
}

main "$@"
