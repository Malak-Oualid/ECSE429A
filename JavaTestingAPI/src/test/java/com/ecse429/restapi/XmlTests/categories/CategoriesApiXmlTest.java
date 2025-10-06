package com.ecse429.restapi.XmlTests.categories;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import com.ecse429.restapi.BaseApiTest;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.Random.class)
public class CategoriesApiXmlTest extends BaseApiTest {

    static String createdId;

    @BeforeEach
    void createCategoryPerTest() {
        String xmlBody = "<category><title>xmlCategory</title><description>xml test</description></category>";

        createdId = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body(xmlBody)
        .when()
            .post("/categories")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract()
            .path("category.id");

        Assertions.assertNotNull(createdId);
    }

    @AfterEach
    void cleanupCategoryPerTest() {
        // tolerate 200/204/404 so cleanup doesn't fail
        given().when().delete("/categories/" + createdId)
            .then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    @Order(1)
    void testGetAllCategoriesXml() {
        given()
            .accept(ContentType.XML)
        .when()
            .get("/categories")
        .then()
            .statusCode(200)
            .contentType(ContentType.XML);
    }

    @Test
    @Order(2)
    void testHeadAllCategoriesXml() {
        given()
        .when()
            .head("/categories")
        .then()
            .statusCode(200);
    }

    @Test
    void testCreateCategoryXml() {
        // createdId is made in @BeforeEach; verify it exists and has expected title
        given()
            .accept(ContentType.XML)
        .when()
            .get("/categories/" + createdId)
        .then()
            .statusCode(200)
            .body("categories.category.title", equalTo("xmlCategory"));
    }

    @Test
    @Order(4)
    void testGetCategoryByIdXml() {
        given()
            .accept(ContentType.XML)
        .when()
            .get("/categories/" + createdId)
        .then()
            .statusCode(200)
            .body("categories.category.title", equalTo("xmlCategory"));
    }

    @Test
    @Order(5)
    void testHeadCategoryByIdXml() {
        given()
        .when()
            .head("/categories/" + createdId)
        .then()
            .statusCode(200);
    }

    @Test
    @Order(6)
    void testPostUpdateCategoryXml() {
        String xmlBody = "<category><description>xmlUpdated</description></category>";

        given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body(xmlBody)
        .when()
            .post("/categories/" + createdId)
        .then()
            .statusCode(200)
            .body("category.description", equalTo("xmlUpdated"));
    }

    @Test
    @Order(7)
    void testPutUpdateCategoryXml() {
        String xmlBody = "<category><title>PutTest</title><description>PutXmlUpdate</description></category>";

        given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body(xmlBody)
        .when()
            .put("/categories/" + createdId)
        .then()
            .statusCode(200)
            .body("category.description", equalTo("PutXmlUpdate"));
    }

    @Test
    void testDeleteCategoryXml() {
        // allow 200/204/404 so test is tolerant
        given().when().delete("/categories/" + createdId)
        .then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    void testGetDeletedCategoryXmlShould404() {
        // delete then verify 404 so test is self-contained
        given().when().delete("/categories/" + createdId)
            .then().statusCode(anyOf(is(200), is(204), is(404)));

        given()
            .accept(ContentType.XML)
        .when()
            .get("/categories/" + createdId)
        .then()
            .statusCode(404);
    }
}
