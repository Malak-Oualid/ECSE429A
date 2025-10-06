package com.ecse429.restapi.XmlTests.todos;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;

import com.ecse429.restapi.BaseApiTest;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.Random.class)
public class TodosApiXmlTest extends BaseApiTest {

    static String createdId;

    @BeforeEach
    void createTodoPerTest() {
        String xmlBody = "<todo><title>xmlTodo</title><doneStatus>false</doneStatus><description>xml test</description></todo>";

        createdId = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body(xmlBody)
        .when()
            .post("/todos")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract()
            .path("todo.id");

        Assertions.assertNotNull(createdId);
    }

    @AfterEach
    void cleanupTodoPerTest() {
        given().when().delete("/todos/" + createdId)
            .then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    void testGetAllTodosXml() {
        given()
            .accept(ContentType.XML)
        .when()
            .get("/todos")
        .then()
            .statusCode(200)
            .contentType(ContentType.XML);
    }

    @Test
    void testHeadAllTodosXml() {
        given()
        .when()
            .head("/todos")
        .then()
            .statusCode(200);
    }

    @Test
    void testCreateTodoXml() {
        given()
            .accept(ContentType.XML)
        .when()
            .get("/todos/" + createdId)
        .then()
            .statusCode(200)
            .body("todos.todo.title", equalTo("xmlTodo"));
    }

    @Test
    void testGetTodoByIdXml() {
        given()
            .accept(ContentType.XML)
        .when()
            .get("/todos/" + createdId)
        .then()
            .statusCode(200)
            .body("todos.todo.title", equalTo("xmlTodo"));
    }

    @Test
    void testHeadTodoByIdXml() {
        given()
        .when()
            .head("/todos/" + createdId)
        .then()
            .statusCode(200);
    }

    @Test
    void testPostUpdateTodoXml() {
        String xmlBody = "<todo><description>xmlUpdated</description></todo>";

        given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body(xmlBody)
        .when()
            .post("/todos/" + createdId)
        .then()
            .statusCode(200)
            .body("todo.description", equalTo("xmlUpdated"));
    }

    @Test
    void testPutUpdateTodoXml() {
        String xmlBody = "<todo><title>PutTest</title><description>PutXmlUpdate</description><doneStatus>true</doneStatus></todo>";

        given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body(xmlBody)
        .when()
            .put("/todos/" + createdId)
        .then()
            .statusCode(200)
            .body("todo.description", equalTo("PutXmlUpdate"));
    }

    @Test
    void testDeleteTodoXml() {
        given()
        .when()
            .delete("/todos/" + createdId)
        .then()
            .statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    void testGetDeletedTodoXmlShould404() {
        given().when().delete("/todos/" + createdId)
            .then().statusCode(anyOf(is(200), is(204), is(404)));

        given()
            .accept(ContentType.XML)
        .when()
            .get("/todos/" + createdId)
        .then()
            .statusCode(404);
    }

    @Test
    void collectionFilters_Smoke() {
        String doneTodoId = given().contentType(ContentType.XML).accept(ContentType.XML)
                .body("<todo><title>doneOne</title><doneStatus>true</doneStatus></todo>")
            .when().post("/todos")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().path("todo.id");

        given().accept(ContentType.XML)
        .when().get("/todos?doneStatus=true")
        .then().statusCode(200).body("todos.todo.id", hasItem(doneTodoId));

        // cleanup created todo
        given().when().delete("/todos/" + doneTodoId).then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    void testMalformedJsonPayload() {
        // Send malformed JSON to an endpoint that accepts JSON to see how server responds
        given()
            .contentType(ContentType.JSON)
            .body("{ this is : not valid json }")
        .when()
            .post("/todos")
        .then()
            // Expect a client error (4xx). Exact status depends on server implementation.
            .statusCode(greaterThanOrEqualTo(400));
    }

    @Test
    void testMalformedXmlPayload() {
        // Send malformed XML to the XML endpoint
        given()
            .contentType(ContentType.XML)
            .body("<todo><title>broken</title><unclosed>")
        .when()
            .post("/todos")
        .then()
            .statusCode(greaterThanOrEqualTo(400));
    }

    @Test
    void testDeleteAlreadyDeleted() {
        // Create a todo
        String id = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body("<todo><title>toBeDeleted</title></todo>")
        .when()
            .post("/todos")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("todo.id");

        // First delete should succeed
        given().when().delete("/todos/" + id).then().statusCode(anyOf(is(200), is(204)));

        // Second delete is an invalid operation; assert it returns 404 (service should indicate missing resource)
        given().when().delete("/todos/" + id).then().statusCode(404);
    }
}
