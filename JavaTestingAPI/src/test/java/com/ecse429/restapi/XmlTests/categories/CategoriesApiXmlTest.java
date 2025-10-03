package com.ecse429.restapi.XmlTests.categories;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import com.ecse429.restapi.BaseApiTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoriesApiXmlTest extends BaseApiTest {

    static String createdId;

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
    @Order(3)
    void testCreateCategoryXml() {
        String xmlBody = "<category><title>xmlCategory</title><description>xml test</description></category>";

        createdId = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body(xmlBody)
        .when()
            .post("/categories")
        .then()
            .statusCode(201)
            .body("category.title", equalTo("xmlCategory"))
            .extract()
            .path("category.id");

        Assertions.assertNotNull(createdId);
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
    @Order(8)
    void testDeleteCategoryXml() {
        given()
        .when()
            .delete("/categories/" + createdId)
        .then()
            .statusCode(200);
    }

    @Test
    @Order(9)
    void testGetDeletedCategoryXmlShould404() {
        given()
            .accept(ContentType.XML)
        .when()
            .get("/categories/" + createdId)
        .then()
            .statusCode(404);
    }
}
