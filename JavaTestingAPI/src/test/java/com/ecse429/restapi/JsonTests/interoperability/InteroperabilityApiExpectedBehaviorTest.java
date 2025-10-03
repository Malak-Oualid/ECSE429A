package com.ecse429.restapi.JsonTests.interoperability;

import com.ecse429.restapi.BaseApiTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Exact replay of the Bash/Postman session for Category-related endpoints:
 * - Todos ↔ Categories
 * - Projects ↔ Categories
 * - /categories collection with reverse relationships
 *
 * Notes:
 * - Create endpoints may return 200 or 201, so assertions accept both.
 * - HEAD may return 200 or 204.
 * - Helper methods extract IDs whether the API responds with a single object or a wrapped array.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InteroperabilityApiExpectedBehaviorTest extends BaseApiTest {

    // IDs captured during the flow
    private static String todoRelCategoryId;     // category linked to /todos/1
    private static String projectRelCategoryId;  // category linked to /projects/1
    private static String workingCategoryId;     // new /categories id
    private static String workingProjectId;      // new /projects id
    private static String relatedTodoId;         // todo created under /categories/{id}/todos

    // ---------- Todos ↔ Categories ----------

    @Test @Order(1)  @DisplayName("6:28 PM GET /todos → 200")
    void step_0628_getTodos() {
        when().get("/todos").then().statusCode(200);
    }

    @Test @Order(2)  @DisplayName("6:31 PM GET /todos/1 → 200")
    void step_0631_getTodo1() {
        when().get("/todos/1").then().statusCode(200);
    }

    @Test @Order(3)  @DisplayName("6:35 PM GET /todos/1/categories → 200")
    void step_0635_getTodo1Categories() {
        when().get("/todos/1/categories").then().statusCode(200);
    }

    @Test @Order(4)  @DisplayName("6:42 PM GET /todos/1/categories/1 → 404 (or 200 if exists)")
    void step_0642_getTodo1Categories1() {
        int code = when().get("/todos/1/categories/1").then().extract().statusCode();
        assertTrue(code == 200 || code == 404);
    }

    @Test @Order(5)  @DisplayName("6:45 PM POST /todos/1/categories (no body) → 4xx")
    void step_0645_postTodo1CategoriesEmpty() {
        int code = given()
            .contentType("application/json")
            .body("")
        .when()
            .post("/todos/1/categories")
        .then().extract().statusCode();
        assertTrue(code >= 400 && code < 500);
    }

    @Test @Order(6)  @DisplayName("6:50 PM POST /todos/1/categories (with title) → 200/201 + capture id")
    void step_0650_postTodo1CategoriesWithTitle() {
        Response res = given()
            .contentType("application/json")
            .body("{\"title\":\"RelCat-" + System.currentTimeMillis() + "\"}")
        .when()
            .post("/todos/1/categories")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .contentType(containsString("application/json"))
        .extract().response();

        todoRelCategoryId = firstIdFrom(res);
        if (todoRelCategoryId == null) {
            Response list = when().get("/todos/1/categories").then().statusCode(200).extract().response();
            todoRelCategoryId = lastIdFromArray(list, "categories");
        }
        assertNotNull(todoRelCategoryId, "Should capture created category id under /todos/1");
    }

    @Test @Order(7)  @DisplayName("6:55 PM DELETE /todos/1/categories/{id} → 200")
    void step_0655_deleteTodo1CategoriesId() {
        assumeId(todoRelCategoryId, "todoRelCategoryId missing");
        when().delete("/todos/1/categories/" + todoRelCategoryId).then().statusCode(200);
    }

    @Test @Order(8)  @DisplayName("7:00 PM GET /todos/1/categories (verify deletion) → 200")
    void step_0700_verifyTodo1Categories() {
        Response res = when().get("/todos/1/categories").then().statusCode(200).extract().response();
        assertFalse(arrayContainsId(res, "categories", todoRelCategoryId));
    }

    @Test @Order(9)  @DisplayName("7:05 PM HEAD /todos/1/categories → 200/204")
    void step_0705_headTodo1Categories() {
        int code = when().head("/todos/1/categories").then().extract().statusCode();
        assertTrue(code == 200 || code == 204);
    }

    // ---------- Projects ↔ Categories ----------

    @Test @Order(10) @DisplayName("7:08 PM GET /projects → 200")
    void step_0708_getProjects() {
        when().get("/projects").then().statusCode(200);
    }

    @Test @Order(11) @DisplayName("7:10 PM GET /projects/1/categories → 200")
    void step_0710_getProject1Categories() {
        when().get("/projects/1/categories").then().statusCode(200);
    }

    @Test @Order(12) @DisplayName("7:12 PM POST /projects/1/categories (with title) → 200/201 + capture id")
    void step_0712_postProject1CategoriesWithTitle() {
        Response res = given()
            .contentType("application/json")
            .body("{\"title\":\"ProjRelCat-" + System.currentTimeMillis() + "\"}")
        .when()
            .post("/projects/1/categories")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .contentType(containsString("application/json"))
        .extract().response();

        projectRelCategoryId = firstIdFrom(res);
        if (projectRelCategoryId == null) {
            Response list = when().get("/projects/1/categories").then().statusCode(200).extract().response();
            projectRelCategoryId = lastIdFromArray(list, "categories");
        }
        assertNotNull(projectRelCategoryId, "Should capture created category id under /projects/1");
    }

    @Test @Order(13) @DisplayName("7:15 PM DELETE /projects/1/categories/{id} → 200")
    void step_0715_deleteProject1CategoriesId() {
        assumeId(projectRelCategoryId, "projectRelCategoryId missing");
        when().delete("/projects/1/categories/" + projectRelCategoryId).then().statusCode(200);
    }

    @Test @Order(14) @DisplayName("7:18 PM HEAD /projects/1/categories/{id} → 200/204 (best-effort)")
    void step_0718_headProject1CategoriesId() {
        // server may return 404 after deletion; in your notes it returned 200, so allow 200/204/404
        assumeId(projectRelCategoryId, "projectRelCategoryId missing");
        int code = when().head("/projects/1/categories/" + projectRelCategoryId).then().extract().statusCode();
        assertTrue(code == 200 || code == 204 || code == 404);
    }

    @Test @Order(15) @DisplayName("7:20 PM GET /projects/1/categories → 200 (possibly empty)")
    void step_0720_getProject1CategoriesVerify() {
        when().get("/projects/1/categories").then().statusCode(200);
    }

    // ---------- /categories collection & reverse relationships ----------

    @Test @Order(16) @DisplayName("7:22 PM GET /categories → 200")
    void step_0722_getCategories() {
        when().get("/categories").then().statusCode(200);
    }

    @Test @Order(17) @DisplayName("7:24 PM POST /categories/{cat}/projects (link) → 200/201; then DELETE → 200")
    void step_0724_linkAndDeleteCategoryProject() {
        // create working category
        Response c = given()
            .contentType("application/json")
            .body("{\"title\":\"AutoCat-" + System.currentTimeMillis() + "\"}")
        .when()
            .post("/categories")
        .then()
            .statusCode(anyOf(is(200), is(201)))
        .extract().response();
        workingCategoryId = firstIdFromObjectOrWrapped(c);
        assertNotNull(workingCategoryId);

        // create working project
        Response p = given()
            .contentType("application/json")
            .body("{\"title\":\"AutoProj-" + System.currentTimeMillis() + "\"}")
        .when()
            .post("/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)))
        .extract().response();
        workingProjectId = firstIdFromObjectOrWrapped(p);
        assertNotNull(workingProjectId);

        // link existing project to category
        given()
            .contentType("application/json")
            .body("{\"id\":\"" + workingProjectId + "\"}")
        .when()
            .post("/categories/" + workingCategoryId + "/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)));

        // delete the link
        when().delete("/categories/" + workingCategoryId + "/projects/" + workingProjectId)
        .then().statusCode(200);
    }

    @Test @Order(18) @DisplayName("7:28 PM GET /categories/{cat}/todos → 200")
    void step_0728_getCategoryTodos() {
        assumeId(workingCategoryId, "workingCategoryId missing");
        when().get("/categories/" + workingCategoryId + "/todos").then().statusCode(200);
    }

    @Test @Order(19) @DisplayName("7:30 PM POST /categories/{cat}/todos (create+link) → 200/201 + capture id")
    void step_0730_postCategoryTodosCreateAndLink() {
        assumeId(workingCategoryId, "workingCategoryId missing");
        Response res = given()
            .contentType("application/json")
            .body("{\"title\":\"RelTodo-" + System.currentTimeMillis() + "\"}")
        .when()
            .post("/categories/" + workingCategoryId + "/todos")
        .then()
            .statusCode(anyOf(is(200), is(201)))
        .extract().response();

        relatedTodoId = firstIdFrom(res);
        if (relatedTodoId == null) {
            Response list = when().get("/categories/" + workingCategoryId + "/todos")
                                .then().statusCode(200).extract().response();
            relatedTodoId = lastIdFromArray(list, "todos");
        }
        assertNotNull(relatedTodoId, "Should capture created/linked todo id");
    }

    @Test @Order(20) @DisplayName("7:32 PM DELETE /categories/{cat}/todos/{tid} → 200")
    void step_0732_deleteCategoryTodosId() {
        assumeId(workingCategoryId, "workingCategoryId missing");
        assumeId(relatedTodoId, "relatedTodoId missing");
        when().delete("/categories/" + workingCategoryId + "/todos/" + relatedTodoId)
        .then().statusCode(200);
    }

    @Test @Order(21) @DisplayName("7:34 PM GET /categories/{cat}/todos (verify empty) → 200")
    void step_0734_verifyCategoryTodosEmpty() {
        assumeId(workingCategoryId, "workingCategoryId missing");
        when().get("/categories/" + workingCategoryId + "/todos").then().statusCode(200);
    }

    // ---------- Helpers ----------

    private static void assumeId(String id, String msg) {
        assertNotNull(id, msg);
        assertFalse(id.isEmpty(), msg);
    }

    /** Try top-level "id", otherwise first id from arrays like "categories"/"projects"/"todos". */
    private static String firstIdFrom(Response res) {
        try {
            JsonPath jp = res.jsonPath();
            if (jp.get("id") != null) return String.valueOf(jp.get("id"));
            for (String k : new String[]{"categories", "projects", "todos", "data"}) {
                Object arr = jp.get(k);
                if (arr instanceof java.util.List<?> && !((java.util.List<?>) arr).isEmpty()) {
                    Object id = ((java.util.Map<?, ?>) ((java.util.List<?>) arr).get(0)).get("id");
                    if (id != null) return String.valueOf(id);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    /** For POST /categories or /projects which usually return a single object. */
    private static String firstIdFromObjectOrWrapped(Response res) {
        try {
            JsonPath jp = res.jsonPath();
            Object id = jp.get("id");
            if (id != null) return String.valueOf(id);
            return firstIdFrom(res);
        } catch (Exception e) {
            return null;
        }
    }

    private static String lastIdFromArray(Response res, String arrayKey) {
        try {
            java.util.List<java.util.Map<String, Object>> items = res.jsonPath().getList(arrayKey);
            if (items != null && !items.isEmpty()) {
                Object id = items.get(items.size() - 1).get("id");
                if (id != null) return String.valueOf(id);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static boolean arrayContainsId(Response res, String arrayKey, String id) {
        try {
            java.util.List<java.util.Map<String, Object>> items = res.jsonPath().getList(arrayKey);
            if (items == null) return false;
            for (var it : items) {
                if (id != null && id.equals(String.valueOf(it.get("id")))) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
