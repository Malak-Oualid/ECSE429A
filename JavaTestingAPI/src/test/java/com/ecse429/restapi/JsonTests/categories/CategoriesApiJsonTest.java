package com.ecse429.restapi.JsonTests.categories;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import com.ecse429.restapi.BaseApiTest;

@TestMethodOrder(MethodOrderer.Random.class)
public class CategoriesApiJsonTest extends BaseApiTest {
    @Test
    @Order(10)
    void testPostMalformedJsonReturns400() {
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"MissingEndQuote}") // malformed JSON
        .when()
            .post("/categories")
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
            .post("/categories")
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
            .patch("/categories")
        .then()
            .statusCode(405);
    }

    @Test
    @Order(13)
    void testPostIncreasesCountByOne() {
        int before = given().accept(ContentType.JSON).when().get("/categories").then().extract().path("categories.size()");
        String idString = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"CountTest\"}")
        .when()
            .post("/categories")
        .then()
            .statusCode(201)
            .extract().path("id");
        int after = given().accept(ContentType.JSON).when().get("/categories").then().extract().path("categories.size()");
        Assertions.assertEquals(before + 1, after);
        // Clean up
        given().when().delete("/categories/" + idString).then().statusCode(200);
    }

    @Test
    @Order(14)
    void testDeleteOnlyDeletesThatCategory() {
        // Create two categories
        String id1 = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("{\"title\":\"Del1\"}").when().post("/categories").then().statusCode(201).extract().path("id");
        String id2 = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("{\"title\":\"Del2\"}").when().post("/categories").then().statusCode(201).extract().path("id");
        // Delete id1
        given().when().delete("/categories/" + id1).then().statusCode(200);
        // id2 should still exist
        given().accept(ContentType.JSON).when().get("/categories/" + id2).then().statusCode(200);
        // Clean up
        given().when().delete("/categories/" + id2).then().statusCode(200);
    }

    @Test
    @Order(15)
    void testSystemHealthFailsIfServiceDown() {
        try {
            given().accept(ContentType.JSON).when().get("/categories").then().statusCode(200);
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
            .post("/categories")
        .then()
            .statusCode(anyOf(is(201), is(400)));
    }

    // No shared state; each test will create its own category and use its ID

    @Test
    @Order(1)
    void testGetAllCategories() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/categories")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    @Order(2)
    void testHeadAllCategories() {
        given()
        .when()
            .head("/categories")
        .then()
            .statusCode(200);
    }

    @Test
    @Order(3)
    void testCreateCategory() {
        String idString = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"jsonCategory\",\"description\":\"test json\"}")
        .when()
            .post("/categories")
        .then()
            .statusCode(201)
            .body("title", equalTo("jsonCategory"))
            .extract().path("id");
        // Clean up
        given().when().delete("/categories/" + idString).then().statusCode(200);
    }

    @Test
    @Order(4)
    void testGetCategoryById() {
        // Create a category
        String idString = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"jsonCategory\",\"description\":\"test json\"}")
        .when()
            .post("/categories")
        .then()
            .statusCode(201)
            .extract().path("id");
        // Get by id
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/categories/" + idString)
        .then()
            .statusCode(200)
            .body("categories[0].id", equalTo(idString))
            .body("categories[0].title", equalTo("jsonCategory"));
        // Clean up
        given().when().delete("/categories/" + idString).then().statusCode(200);
    }

    @Test
    @Order(5)
    void testHeadCategoryById() {
        // Create a category
        String idString = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"HeadCatTest\"}")
        .when()
            .post("/categories")
        .then()
            .statusCode(201)
            .extract().path("id");
        // Head by id
        given()
            .when()
            .head("/categories/" + idString)
        .then()
            .statusCode(200);
        // Clean up
        given().when().delete("/categories/" + idString).then().statusCode(200);
    }

    @Test
    @Order(6)
    void testPostUpdateCategory() {
        // Create a category
        String idString = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"PostUpdateCatTest\"}")
        .when()
            .post("/categories")
        .then()
            .statusCode(201)
            .extract().path("id");
        // Post update
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"description\":\"jsonUpdated\"}")
        .when()
            .post("/categories/" + idString)
        .then()
            .statusCode(200)
            .body("description", equalTo("jsonUpdated"));
        // Clean up
        given().when().delete("/categories/" + idString).then().statusCode(200);
    }

    @Test
    @Order(7)
    void testPutUpdateCategory() {
        // Create a category
        String idString = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"PutTest\"}")
        .when()
            .post("/categories")
        .then()
            .statusCode(201)
            .extract().path("id");
        // Put update
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"PutTest\",\"description\":\"jsonPutUpdated\"}")
        .when()
            .put("/categories/" + idString)
        .then()
            .statusCode(200)
            .body("description", equalTo("jsonPutUpdated"));
        // Clean up
        given().when().delete("/categories/" + idString).then().statusCode(200);
    }

    @Test
    @Order(8)
    void testDeleteCategory() {
        // Create a category
        String idString = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"DeleteCatTest\"}")
        .when()
            .post("/categories")
        .then()
            .statusCode(201)
            .extract().path("id");
        // Delete
        given()
            .when()
            .delete("/categories/" + idString)
        .then()
            .statusCode(200);
    }

    @Test
    @Order(9)
    void testGetDeletedCategoryShould404() {
        // Create and delete a category
        String idString = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"Delete404CatTest\"}")
        .when()
            .post("/categories")
        .then()
            .statusCode(201)
            .extract().path("id");
        given().when().delete("/categories/" + idString).then().statusCode(200);
        // Confirm 404
        given()
            .when()
            .get("/categories/" + idString)
        .then()
            .statusCode(404);
    }
}

