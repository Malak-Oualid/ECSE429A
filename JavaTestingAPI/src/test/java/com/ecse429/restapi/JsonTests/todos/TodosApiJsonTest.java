package com.ecse429.restapi.JsonTests.todos;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import com.ecse429.restapi.BaseApiTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TodosApiJsonTest extends BaseApiTest {

    static String createdId;

    @Test
    @Order(1)
    void testGetAllTodos() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/todos")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    @Order(2)
    void testHeadAllTodos() {
        given()
        .when()
            .head("/todos")
        .then()
            .statusCode(200);
    }

    @Test
    @Order(3)
    void testCreateTodo() {
        String body = "{\"title\":\"empor incididunt uta\",\"doneStatus\":false,\"description\":\"exercitation ullamco\"}";

        createdId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
        .when()
            .post("/todos")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .contentType(ContentType.JSON)
            .body("title", equalTo("empor incididunt uta"))
            .body("description", equalTo("exercitation ullamco"))
            .extract()
            .path("id");

        Assertions.assertNotNull(createdId, "Created todo ID should not be null");
    }

    @Test
    @Order(4)
    void testGetTodoById() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/todos/" + createdId)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    @Order(5)
    void testHeadTodoById() {
        given()
        .when()
            .head("/todos/" + createdId)
        .then()
            .statusCode(200);
    }

    @Test
    @Order(6)
    void testPostUpdateTodo() {
        String update = "{\"description\":\"updated description via POST\"}";
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(update)
        .when()
            .post("/todos/" + createdId)
        .then()
            .statusCode(200)
            .body("description", equalTo("updated description via POST"));
    }

    @Test
    @Order(7)
    void testPutUpdateTodo() {
        String update = "{\"title\":\"testPutUpdateTodo\",\"description\":\"updated description via PUT\"}";
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(update)
        .when()
            .put("/todos/" + createdId)
        .then()
            .statusCode(200)
            .body("description", equalTo("updated description via PUT"));
    }

    @Test
    @Order(8)
    void testTodosCategoriesRelationshipCrud() {
        // Create a category to relate to
        String catBody = "{\"title\":\"todoCatForRel\",\"description\":\"rel test\"}";
        String catId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(catBody)
        .when()
            .post("/categories")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract()
            .path("id");

        // Create the relationship: POST /todos/:id/categories with { "id": "<catId>" }
        String relBody = String.format("{\"id\":\"%s\"}", catId);
        given()
            .contentType(ContentType.JSON)
            .body(relBody)
        .when()
            .post("/todos/" + createdId + "/categories")
        .then()
            .statusCode(anyOf(is(200), is(201)));

        // Verify relationship exists via GET /todos/:id/categories
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/todos/" + createdId + "/categories")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);

        // Cleanup relationship
        given()
        .when()
            .delete("/todos/" + createdId + "/categories/" + catId)
        .then()
            .statusCode(anyOf(is(200), is(204)));

        // Cleanup category
        given()
        .when()
            .delete("/categories/" + catId)
        .then()
            .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    @Order(9)
    void testDeleteTodo() {
        given()
        .when()
            .delete("/todos/" + createdId)
        .then()
            .statusCode(anyOf(is(200), is(204)));

        // verify deleted
        given()
        .when()
            .get("/todos/" + createdId)
        .then()
            .statusCode(404);
    }
}
