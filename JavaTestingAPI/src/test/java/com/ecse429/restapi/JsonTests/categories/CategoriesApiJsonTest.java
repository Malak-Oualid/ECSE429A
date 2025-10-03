package com.ecse429.restapi.JsonTests.categories;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import com.ecse429.restapi.BaseApiTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoriesApiJsonTest extends BaseApiTest {

    static int createdId;

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
            .extract()
            .path("id");

        createdId = Integer.parseInt(idString);
        System.out.println(createdId);
        Assertions.assertTrue(createdId > 0);
    }

    @Test
    @Order(4)
    void testGetCategoryById() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/categories/" + createdId)
        .then()
            .statusCode(200)
            .body("categories[0].id", equalTo(String.valueOf(createdId)))
            .body("categories[0].title", equalTo("jsonCategory"));
    }

    @Test
    @Order(5)
    void testHeadCategoryById() {
        given()
        .when()
            .head("/categories/" + createdId)
        .then()
            .statusCode(200);
    }

    @Test
    @Order(6)
    void testPostUpdateCategory() {
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"description\":\"jsonUpdated\"}")
        .when()
            .post("/categories/" + createdId)
        .then()
            .statusCode(200)
            .body("description", equalTo("jsonUpdated"));
    }

    @Test
    @Order(7)
    void testPutUpdateCategory() {
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"PutTest\",\"description\":\"jsonPutUpdated\"}")
        .when()
            .put("/categories/" + createdId)
        .then()
            .statusCode(200)
            .body("description", equalTo("jsonPutUpdated"));
    }

    @Test
    @Order(8)
    void testDeleteCategory() {
        given()
        .when()
            .delete("/categories/" + createdId)
        .then()
            .statusCode(200);
    }

    @Test
    @Order(9)
    void testGetDeletedCategoryShould404() {
        given()
        .when()
            .get("/categories/" + createdId)
        .then()
            .statusCode(404);
    }
}

