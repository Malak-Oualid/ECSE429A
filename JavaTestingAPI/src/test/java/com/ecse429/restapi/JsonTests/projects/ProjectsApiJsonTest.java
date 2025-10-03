package com.ecse429.restapi.JsonTests.projects;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import com.ecse429.restapi.BaseApiTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsApiJsonTest extends BaseApiTest {

    static String createdId;

    @Test
    @Order(1)
    void testGetAllProjects() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/projects")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    @Order(2)
    void testHeadAllProjects() {
        given()
        .when()
            .head("/projects")
        .then()
            .statusCode(200);
    }

    @Test
    @Order(3)
    void testCreateProject() {
        String body = "{\"title\":\"runt mollit anim ida\",\"completed\":false,\"active\":true,\"description\":\"do eiusmod tempor ia\"}";

        createdId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
        .when()
            .post("/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .contentType(ContentType.JSON)
            .body("title", equalTo("runt mollit anim ida"))
            .extract()
            .path("id");

        Assertions.assertNotNull(createdId);
    }

    @Test
    @Order(4)
    void testGetProjectById() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/projects/" + createdId)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("projects[0].id", equalTo(createdId))
            .body("projects[0].title", equalTo("runt mollit anim ida"));
    }

    @Test
    @Order(5)
    void testHeadProjectById() {
        given()
        .when()
            .head("/projects/" + createdId)
        .then()
            .statusCode(200);
    }

    @Test
    @Order(6)
    void testPostUpdateProject() {
        String update = "{\"description\":\"updated project desc via POST\"}";
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(update)
        .when()
            .post("/projects/" + createdId)
        .then()
            .statusCode(200)
            .body("description", equalTo("updated project desc via POST"));
    }

    @Test
    @Order(7)
    void testPutUpdateProject() {
        String update = "{\"description\":\"updated project desc via PUT\"}";
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(update)
        .when()
            .put("/projects/" + createdId)
        .then()
            .statusCode(200)
            .body("description", equalTo("updated project desc via PUT"));
    }

    @Test
    @Order(8)
    void testProjectsCategoriesRelationshipCrud() {
        // Create a category to relate to
        String catBody = "{\"title\":\"projCatForRel\",\"description\":\"proj rel test\"}";
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

        // Create relationship: POST /projects/:id/categories with { "id": "<catId>" }
        String relBody = String.format("{\"id\":\"%s\"}", catId);
        given()
            .contentType(ContentType.JSON)
            .body(relBody)
        .when()
            .post("/projects/" + createdId + "/categories")
        .then()
            .statusCode(anyOf(is(200), is(201)));

        // Verify relationship via GET /projects/:id/categories
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/projects/" + createdId + "/categories")
        .then()
            .statusCode(200);
        // Cleanup relationship and category
        given()
        .when()
            .delete("/projects/" + createdId + "/categories/" + catId)
        .then()
            .statusCode(anyOf(is(200), is(204)));

        given()
        .when()
            .delete("/categories/" + catId)
        .then()
            .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    @Order(9)
    void testDeleteProject() {
        given()
        .when()
            .delete("/projects/" + createdId)
        .then()
            .statusCode(anyOf(is(200), is(204)));

        // verify deleted
        given()
        .when()
            .get("/projects/" + createdId)
        .then()
            .statusCode(404);
    }
}

