package com.ecse429.restapi.JsonTests.todos;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@TestMethodOrder(MethodOrderer.Random.class)

public class todosEndpoint extends com.ecse429.restapi.BaseApiTest {

    // No fixed IDs needed; each test will create its own todo and use its ID

    @Test
    @Order(1)
    void testGetAllToDos() {
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
    void testHeadTodos() {
        given()
            .when()
            .head("/todos")
        .then()
            .statusCode(200);
    }

    @Test
    @Order(3)
    void testCreateToDo() {
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"jsonToDo\",\"description\":\"test json\"}")
        .when()
            .post("/todos")
        .then()
            .statusCode(201)
            .body("title", equalTo("jsonToDo"));
    }

    @Test
    @Order(4)
    void testGetToDoById() {
        // Create a todo
        String idString = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"GetByIdTest\",\"description\":\"desc\"}")
        .when()
            .post("/todos")
        .then()
            .statusCode(201)
            .extract().path("id");
        // Get by id
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/todos/" + idString)
        .then()
            .statusCode(200)
            .body("todos[0].id", equalTo(idString));
        // Clean up
        given().when().delete("/todos/" + idString).then().statusCode(200);
    }

    @Test
    @Order(5)
    void testHeadToDoById() {
        // Create a todo
        String idString = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"HeadByIdTest\"}")
        .when()
            .post("/todos")
        .then()
            .statusCode(201)
            .extract().path("id");
        // Head by id
        given()
            .when()
            .head("/todos/" + idString)
        .then()
            .statusCode(200);
        // Clean up
        given().when().delete("/todos/" + idString).then().statusCode(200);
    }

    @Test
    @Order(6)
    void testPostUpdateToDo() {
        // Create a todo
        String idString = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"PostUpdateTest\"}")
        .when()
            .post("/todos")
        .then()
            .statusCode(201)
            .extract().path("id");
        // Post update
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"description\":\"jsonUpdated\"}")
        .when()
            .post("/todos/" + idString)
        .then()
            .statusCode(200)
            .body("description", equalTo("jsonUpdated"));
        // Clean up
        given().when().delete("/todos/" + idString).then().statusCode(200);
    }

    @Test
    @Order(7)
    void testPutUpdateToDo() {
        // Create a todo
        String idString = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"PutTest\"}")
        .when()
            .post("/todos")
        .then()
            .statusCode(201)
            .extract().path("id");
        // Put update
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"PutTest\",\"description\":\"jsonPutUpdated\"}")
        .when()
            .put("/todos/" + idString)
        .then()
            .statusCode(200)
            .body("description", equalTo("jsonPutUpdated"));
        // Clean up
        given().when().delete("/todos/" + idString).then().statusCode(200);
    }

    @Test
    @Order(8)
    void testDeleteToDo() {
        // Create a todo
        String idString = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"DeleteTest\"}")
        .when()
            .post("/todos")
        .then()
            .statusCode(201)
            .extract().path("id");
        // Delete
        given()
            .when()
            .delete("/todos/" + idString)
        .then()
            .statusCode(200);
    }

    @Test
    @Order(9)
    void testGetDeletedToDoShould404() {
        // Create and delete a todo
        String idString = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"Delete404Test\"}")
        .when()
            .post("/todos")
        .then()
            .statusCode(201)
            .extract().path("id");
        given().when().delete("/todos/" + idString).then().statusCode(200);
        // Confirm 404
        given()
            .when()
            .get("/todos/" + idString)
        .then()
            .statusCode(404);
    }
}
