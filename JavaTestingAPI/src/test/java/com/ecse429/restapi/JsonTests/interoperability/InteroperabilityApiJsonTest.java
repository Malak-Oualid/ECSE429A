package com.ecse429.restapi.JsonTests.interoperability;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;

import com.ecse429.restapi.BaseApiTest;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // allow non-static @BeforeAll/@AfterAll
public class InteroperabilityApiJsonTest extends BaseApiTest {

    private String todoId;
    private String projectId;
    private String categoryId;

    @BeforeAll
    void createSharedEntities() {
        // create todo
        todoId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"interopTodo\",\"doneStatus\":false,\"description\":\"interop todo\"}")
        .when()
            .post("/todos")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");
        Assertions.assertNotNull(todoId, "todoId should not be null");

        // create project
        projectId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"interopProject\",\"completed\":false,\"active\":true,\"description\":\"interop project\"}")
        .when()
            .post("/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");
        Assertions.assertNotNull(projectId, "projectId should not be null");

        // create category
        categoryId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"interopCategory\",\"description\":\"interop category\"}")
        .when()
            .post("/categories")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");
        Assertions.assertNotNull(categoryId, "categoryId should not be null");
    }

    @AfterAll
    void cleanupAll() {
        // Best-effort deletes: allow 200/204/404 so cleanup doesn’t fail the suite
        safeDelete(String.format("/todos/%s/categories/%s", todoId, categoryId));
        safeDelete(String.format("/projects/%s/categories/%s", projectId, categoryId));
        safeDelete(String.format("/projects/%s/tasks/%s", projectId, todoId));

        safeDelete("/todos/" + todoId);
        safeDelete("/projects/" + projectId);
        safeDelete("/categories/" + categoryId);
    }

    private void safeDelete(String path) {
        given().when().delete(path)
            .then()
            .statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    void linkTodoToProjectViaTasksAndVerify() {
        // Create relationship: POST /projects/:id/tasks with { "id": "<todoId>" }
        String relBody = String.format("{\"id\":\"%s\"}", todoId);

        given()
            .contentType(ContentType.JSON)
            .body(relBody)
        .when()
            .post("/projects/" + projectId + "/tasks")
        .then()
            .statusCode(anyOf(is(200), is(201)));

        // Verify tasks under project
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/projects/" + projectId + "/tasks")
        .then()
            .statusCode(200)
            .body("todos.id", hasItem(todoId));

        // Verify project appears in todo tasksof
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/todos/" + todoId + "/tasksof")
        .then()
            .statusCode(200)
            .body("projects.id", hasItem(projectId));
    }

    @Test
    void linkTodoToCategoryAndProjectToCategoryAndVerify() {
        String relBodyCat = String.format("{\"id\":\"%s\"}", categoryId);

        // Link todo -> category
        given()
            .contentType(ContentType.JSON)
            .body(relBodyCat)
        .when()
            .post("/todos/" + todoId + "/categories")
        .then()
            .statusCode(anyOf(is(200), is(201)));

        // Verify via GET /todos/:id/categories
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/todos/" + todoId + "/categories")
        .then()
            .statusCode(200)
            .body("categories.id", hasItem(categoryId));

        // Link project -> category
        given()
            .contentType(ContentType.JSON)
            .body(relBodyCat)
        .when()
            .post("/projects/" + projectId + "/categories")
        .then()
            .statusCode(anyOf(is(200), is(201)));

        // Verify via GET /projects/:id/categories
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/projects/" + projectId + "/categories")
        .then()
            .statusCode(200)
            .body("categories.id", hasItem(categoryId));
    }
    @Test
    void linkProjectFromTodoSideAndVerify() {
        // POST /todos/:id/tasksof with {"id":"<projectId>"}
        given()
            .contentType(ContentType.JSON)
            .body("{\"id\":\"" + projectId + "\"}")
        .when()
            .post("/todos/" + todoId + "/tasksof")
        .then()
            .statusCode(anyOf(is(200), is(201)));

        // Verify both views
        given().accept(ContentType.JSON)
        .when().get("/todos/" + todoId + "/tasksof")
        .then().statusCode(200).body("projects.id", hasItem(projectId));

        given().accept(ContentType.JSON)
        .when().get("/projects/" + projectId + "/tasks")
        .then().statusCode(200).body("todos.id", hasItem(todoId));
    }

    @Test
    void categorySideMirrors_TodosAndProjects() {
        // Link category -> todo
        given()
            .contentType(ContentType.JSON)
            .body("{\"id\":\"" + todoId + "\"}")
        .when()
            .post("/categories/" + categoryId + "/todos")
        .then()
            .statusCode(anyOf(is(200), is(201)));

        // Link category -> project
        given()
            .contentType(ContentType.JSON)
            .body("{\"id\":\"" + projectId + "\"}")
        .when()
            .post("/categories/" + categoryId + "/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)));

        // Verify mirrors
        given().accept(ContentType.JSON)
        .when().get("/categories/" + categoryId + "/todos")
        .then().statusCode(200).body("todos.id", hasItem(todoId));

        given().accept(ContentType.JSON)
        .when().get("/categories/" + categoryId + "/projects")
        .then().statusCode(200).body("projects.id", hasItem(projectId));
    }

    @Test
    void headEndpoints_Smoke() {
        // Collections
        given().when().head("/todos").then().statusCode(200);
        given().when().head("/projects").then().statusCode(200);
        given().when().head("/categories").then().statusCode(200);

        // Instances
        given().when().head("/todos/" + todoId).then().statusCode(200);
        given().when().head("/projects/" + projectId).then().statusCode(200);
        given().when().head("/categories/" + categoryId).then().statusCode(200);

        // Relationships
        given().when().head("/projects/" + projectId + "/tasks").then().statusCode(200);
        given().when().head("/todos/" + todoId + "/tasksof").then().statusCode(200);
        given().when().head("/todos/" + todoId + "/categories").then().statusCode(200);
        given().when().head("/projects/" + projectId + "/categories").then().statusCode(200);
        given().when().head("/categories/" + categoryId + "/todos").then().statusCode(200);
        given().when().head("/categories/" + categoryId + "/projects").then().statusCode(200);
    }

    @Test
    void collectionFilters_Smoke() {
        // Create a done todo so the filter has something to find
        String doneTodoId =
            given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body("{\"title\":\"doneOne\",\"doneStatus\":true}")
            .when().post("/todos")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        // Filter
        given().accept(ContentType.JSON)
        .when().get("/todos?doneStatus=true")
        .then().statusCode(200).body("todos.id", hasItem(doneTodoId));

        // Basic project filter example (adjust to your data)
        given().accept(ContentType.JSON)
        .when().get("/projects?active=true")
        .then().statusCode(200);

        // Cleanup created todo
        given().when().delete("/todos/" + doneTodoId)
        .then().statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void amendEndpoints_PutAndPost() {
        // PUT update todo
        var resp = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"interopTodoUpdated\",\"doneStatus\":true}")
            .log().ifValidationFails()
        .when()
            .put("/todos/" + todoId)
        .then()
            .log().ifValidationFails()
            .statusCode(anyOf(is(200), is(201)))
            .extract()
            .response();

        // Parse flexibly
        io.restassured.path.json.JsonPath jp = resp.jsonPath();

        // Title can be at root or under todos[0]
        String title = jp.getString("title");
        if (title == null) title = jp.getString("todos[0].title");
        Assertions.assertEquals("interopTodoUpdated", title, "title not updated");

        // doneStatus can be boolean or string; at root or todos[0]
        Object ds = jp.get("doneStatus");
        if (ds == null) ds = jp.get("todos[0].doneStatus");
        Assertions.assertNotNull(ds, "doneStatus not found in response");

        boolean done =
            (ds instanceof Boolean) ? (Boolean) ds
            : Boolean.parseBoolean(String.valueOf(ds));
        Assertions.assertTrue(done, "doneStatus should be true but was: " + ds);
    }


    @Test
    void deleteFromMirrorSides() {
        // DELETE from category side
        given().when().delete("/categories/" + categoryId + "/todos/" + todoId)
        .then().statusCode(anyOf(is(200), is(204), is(404)));

        given().when().delete("/categories/" + categoryId + "/projects/" + projectId)
        .then().statusCode(anyOf(is(200), is(204), is(404)));

        // DELETE from todo side for tasksof
        given().when().delete("/todos/" + todoId + "/tasksof/" + projectId)
        .then().statusCode(anyOf(is(200), is(204), is(404)));
    }

}
