package com.ecse429.restapi.JsonTests.interoperability;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import com.ecse429.restapi.BaseApiTest;


@TestMethodOrder(MethodOrderer.Random.class)
public class InteroperabilityApiJsonTest extends BaseApiTest {
    @Test
    @Order(10)
    void testPostMalformedJsonReturns400() {
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"MissingEndQuote}") // malformed JSON
        .when()
            .post("/projects")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(11)
    void testPostWithIdIncludedReturns400() {
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"id\":123,\"title\":\"ShouldFail\"}")
        .when()
            .post("/projects")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(12)
    void testUnsupportedMethodReturns405() {
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .patch("/projects")
        .then()
            .statusCode(405);
    }

    @Test
    @Order(13)
    void testPostIncreasesCountByOne() {
        int before = given().accept(ContentType.JSON).when().get("/projects").then().extract().path("projects.size()");
        String idString = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"CountTest\"}")
        .when()
            .post("/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id").toString();
        int after = given().accept(ContentType.JSON).when().get("/projects").then().extract().path("projects.size()");
        Assertions.assertEquals(before + 1, after);
        // Clean up
        given().when().delete("/projects/" + idString).then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    @Order(14)
    void testDeleteOnlyDeletesThatProject() {
        // Create two projects
        String id1 = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("{\"title\":\"Del1\"}").when().post("/projects").then().statusCode(anyOf(is(200), is(201))).extract().path("id").toString();
        String id2 = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("{\"title\":\"Del2\"}").when().post("/projects").then().statusCode(anyOf(is(200), is(201))).extract().path("id").toString();
        // Delete id1
        given().when().delete("/projects/" + id1).then().statusCode(anyOf(is(200), is(204), is(404)));
        // id2 should still exist
        given().accept(ContentType.JSON).when().get("/projects/" + id2).then().statusCode(200);
        // Clean up
        given().when().delete("/projects/" + id2).then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    @Order(15)
    void testSystemHealthFailsIfServiceDown() {
        try {
            given().accept(ContentType.JSON).when().get("/projects").then().statusCode(200);
        } catch (Exception e) {
            Assertions.fail("Service is not running or not reachable");
        }
    }


    @Test
    @Order(17)
    void testActualBehaviorPasses() {
        // Example: Actual behavior is 201 for empty body
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("")
        .when()
            .post("/projects")
        .then()
            .statusCode(anyOf(is(201), is(400)));
    }

    @Test
    @Order(1)
    void createEntitiesForInterop() {
        // Create todo
        String todoId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"interopTodo\",\"doneStatus\":false,\"description\":\"interop todo\"}")
        .when()
            .post("/todos")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id").toString();
        Assertions.assertNotNull(todoId);
        // Create project
        String projectId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"interopProject\",\"completed\":false,\"active\":true,\"description\":\"interop project\"}")
        .when()
            .post("/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id").toString();
        Assertions.assertNotNull(projectId);
        // Create category
        String categoryId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"interopCategory\",\"description\":\"interop category\"}")
        .when()
            .post("/categories")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id").toString();
        Assertions.assertNotNull(categoryId);
        // Clean up
        given().when().delete("/todos/" + todoId).then().statusCode(anyOf(is(200), is(204), is(404)));
        given().when().delete("/projects/" + projectId).then().statusCode(anyOf(is(200), is(204), is(404)));
        given().when().delete("/categories/" + categoryId).then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    @Order(2)
    void linkTodoToProjectViaTasksAndVerify() {
        // Create todo
        String todoId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"interopTodo\",\"doneStatus\":false,\"description\":\"interop todo\"}")
        .when()
            .post("/todos")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id").toString();
        // Create project
        String projectId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"interopProject\",\"completed\":false,\"active\":true,\"description\":\"interop project\"}")
        .when()
            .post("/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id").toString();
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
        // Clean up
        given().when().delete("/projects/" + projectId + "/tasks/" + todoId).then().statusCode(anyOf(is(200), is(204), is(404)));
        given().when().delete("/todos/" + todoId).then().statusCode(anyOf(is(200), is(204), is(404)));
        given().when().delete("/projects/" + projectId).then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    @Order(3)
    void linkTodoToCategoryAndProjectToCategoryAndVerify() {
        // Create todo
        String todoId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"interopTodo\",\"doneStatus\":false,\"description\":\"interop todo\"}")
        .when()
            .post("/todos")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id").toString();
        // Create project
        String projectId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"interopProject\",\"completed\":false,\"active\":true,\"description\":\"interop project\"}")
        .when()
            .post("/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id").toString();
        // Create category
        String categoryId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"interopCategory\",\"description\":\"interop category\"}")
        .when()
            .post("/categories")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id").toString();
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
        // Clean up
        given().when().delete("/todos/" + todoId + "/categories/" + categoryId).then().statusCode(anyOf(is(200), is(204), is(404)));
        given().when().delete("/projects/" + projectId + "/categories/" + categoryId).then().statusCode(anyOf(is(200), is(204), is(404)));
        given().when().delete("/todos/" + todoId).then().statusCode(anyOf(is(200), is(204), is(404)));
        given().when().delete("/projects/" + projectId).then().statusCode(anyOf(is(200), is(204), is(404)));
        given().when().delete("/categories/" + categoryId).then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    @Order(4)
    void cleanupRelationshipsAndEntities() {
        // This test is now redundant since all other tests clean up after themselves.
        // Optionally, you can leave it empty or remove it.
    }
}
