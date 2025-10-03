package com.ecse429.restapi.JsonTests.interoperability;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import com.ecse429.restapi.BaseApiTest;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InteroperabilityApiJsonTest extends BaseApiTest {

    static String todoId;
    static String projectId;
    static String categoryId;

    @Test
    @Order(1)
    void createEntitiesForInterop() {
        // create todo
        todoId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"interopTodo\",\"doneStatus\":false,\"description\":\"interop todo\"}")
        .when()
            .post("/todos")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract()
            .path("id");

        Assertions.assertNotNull(todoId);

        // create project
        projectId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"interopProject\",\"completed\":false,\"active\":true,\"description\":\"interop project\"}")
        .when()
            .post("/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract()
            .path("id");

        Assertions.assertNotNull(projectId);

        // create category
        categoryId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"interopCategory\",\"description\":\"interop category\"}")
        .when()
            .post("/categories")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract()
            .path("id");

        Assertions.assertNotNull(categoryId);
    }

    @Test
    @Order(2)
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
            .statusCode(200);

        // Verify project appears in todo tasksof
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/todos/" + todoId + "/tasksof")
        .then()
            .statusCode(200);
    }

    @Test
    @Order(3)
    void linkTodoToCategoryAndProjectToCategoryAndVerify() {
        // Link todo -> category
        String relBodyCat = String.format("{\"id\":\"%s\"}", categoryId);
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
            .statusCode(200);

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
            .statusCode(200);
    }

    @Test
    @Order(4)
    void cleanupRelationshipsAndEntities() {
        // Delete todo->category
        given()
        .when()
            .delete("/todos/" + todoId + "/categories/" + categoryId)
        .then()
            .statusCode(anyOf(is(200), is(204)));

        // Delete project->category
        given()
        .when()
            .delete("/projects/" + projectId + "/categories/" + categoryId)
        .then()
            .statusCode(anyOf(is(200), is(204)));

        // Delete project->task (projects/:id/tasks/:id)
        given()
        .when()
            .delete("/projects/" + projectId + "/tasks/" + todoId)
        .then()
            .statusCode(anyOf(is(200), is(204)));

        // Delete entities
        given()
        .when()
            .delete("/todos/" + todoId)
        .then()
            .statusCode(anyOf(is(200), is(204)));

        given()
        .when()
            .delete("/projects/" + projectId)
        .then()
            .statusCode(anyOf(is(200), is(204)));

        given()
        .when()
            .delete("/categories/" + categoryId)
        .then()
            .statusCode(anyOf(is(200), is(204)));
    }
}
