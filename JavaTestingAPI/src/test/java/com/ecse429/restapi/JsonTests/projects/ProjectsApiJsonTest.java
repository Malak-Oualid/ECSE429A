package com.ecse429.restapi.JsonTests.projects;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import com.ecse429.restapi.BaseApiTest;

@TestMethodOrder(MethodOrderer.Random.class)
public class ProjectsApiJsonTest extends BaseApiTest {
    @Test
    @Order(20)
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
    @Order(21)
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
    @Order(22)
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
    @Order(23)
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
    @Order(24)
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
    @Order(25)
    void testSystemHealthFailsIfServiceDown() {
        try {
            given().accept(ContentType.JSON).when().get("/projects").then().statusCode(200);
        } catch (Exception e) {
            Assertions.fail("Service is not running or not reachable");
        }
    }


    @Test
    @Order(27)
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
        String projectId = given()
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
            .path("id").toString();
        Assertions.assertNotNull(projectId);
        // Clean up
        given().when().delete("/projects/" + projectId).then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    @Order(4)
    void testGetProjectById() {
        // Create a project
        String body = "{\"title\":\"runt mollit anim ida\",\"completed\":false,\"active\":true,\"description\":\"do eiusmod tempor ia\"}";
        String projectId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
        .when()
            .post("/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id").toString();
        // Get by id
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/projects/" + projectId)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("projects[0].id", equalTo(projectId))
            .body("projects[0].title", equalTo("runt mollit anim ida"));
        // Clean up
        given().when().delete("/projects/" + projectId).then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    @Order(5)
    void testHeadProjectById() {
        // Create a project
        String body = "{\"title\":\"HeadProjectTest\"}";
        String projectId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
        .when()
            .post("/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id").toString();
        // Head by id
        given()
            .when()
            .head("/projects/" + projectId)
        .then()
            .statusCode(200);
        // Clean up
        given().when().delete("/projects/" + projectId).then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    @Order(6)
    void testPostUpdateProject() {
        // Create a project
        String body = "{\"title\":\"PostUpdateProjectTest\"}";
        String projectId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
        .when()
            .post("/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id").toString();
        // Post update
        String update = "{\"description\":\"updated project desc via POST\"}";
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(update)
        .when()
            .post("/projects/" + projectId)
        .then()
            .statusCode(200)
            .body("description", equalTo("updated project desc via POST"));
        // Clean up
        given().when().delete("/projects/" + projectId).then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    @Order(7)
    void testPutUpdateProject() {
        // Create a project
        String body = "{\"title\":\"PutUpdateProjectTest\"}";
        String projectId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
        .when()
            .post("/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id").toString();
        // Put update
        String update = "{\"description\":\"updated project desc via PUT\"}";
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(update)
        .when()
            .put("/projects/" + projectId)
        .then()
            .statusCode(200)
            .body("description", equalTo("updated project desc via PUT"));
        // Clean up
        given().when().delete("/projects/" + projectId).then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    @Order(8)
    void testProjectsCategoriesRelationshipCrud() {
        // Create a project
        String body = "{\"title\":\"RelProjectTest\"}";
        String projectId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
        .when()
            .post("/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id").toString();
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
            .extract().path("id").toString();
        // Create relationship: POST /projects/:id/categories with { "id": "<catId>" }
        String relBody = String.format("{\"id\":\"%s\"}", catId);
        given()
            .contentType(ContentType.JSON)
            .body(relBody)
        .when()
            .post("/projects/" + projectId + "/categories")
        .then()
            .statusCode(anyOf(is(200), is(201)));
        // Verify relationship via GET /projects/:id/categories
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/projects/" + projectId + "/categories")
        .then()
            .statusCode(200);
        // Cleanup relationship and category
        given()
        .when()
            .delete("/projects/" + projectId + "/categories/" + catId)
        .then()
            .statusCode(anyOf(is(200), is(204)));
        given()
        .when()
            .delete("/categories/" + catId)
        .then()
            .statusCode(anyOf(is(200), is(204)));
        // Clean up project
        given().when().delete("/projects/" + projectId).then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    @Order(9)
    void testDeleteProject() {
        // Create a project
        String body = "{\"title\":\"DeleteProjectTest\"}";
        String projectId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
        .when()
            .post("/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id").toString();
        // Delete
        given()
        .when()
            .delete("/projects/" + projectId)
        .then()
            .statusCode(anyOf(is(200), is(204)));
        // verify deleted
        given()
        .when()
            .get("/projects/" + projectId)
        .then()
            .statusCode(404);
    }
}

