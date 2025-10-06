package com.ecse429.restapi.XmlTests.projects;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;

import com.ecse429.restapi.BaseApiTest;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.Random.class)
public class ProjectsApiXmlTest extends BaseApiTest {

    static String createdId;

    @BeforeEach
    void createProjectPerTest() {
        String xmlBody = "<project><title>xmlProject</title><completed>false</completed><active>true</active><description>xml test</description></project>";

        createdId = given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body(xmlBody)
        .when()
            .post("/projects")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract()
            .path("project.id");

        Assertions.assertNotNull(createdId);
    }

    @AfterEach
    void cleanupProjectPerTest() {
        given().when().delete("/projects/" + createdId)
            .then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    void testGetAllProjectsXml() {
        given()
            .accept(ContentType.XML)
        .when()
            .get("/projects")
        .then()
            .statusCode(200)
            .contentType(ContentType.XML);
    }

    @Test
    void testHeadAllProjectsXml() {
        given()
        .when()
            .head("/projects")
        .then()
            .statusCode(200);
    }

    @Test
    void testCreateProjectXml() {
        given()
            .accept(ContentType.XML)
        .when()
            .get("/projects/" + createdId)
        .then()
            .statusCode(200)
            .body("projects.project.title", equalTo("xmlProject"));
    }

    @Test
    void testGetProjectByIdXml() {
        given()
            .accept(ContentType.XML)
        .when()
            .get("/projects/" + createdId)
        .then()
            .statusCode(200)
            .body("projects.project.title", equalTo("xmlProject"));
    }

    @Test
    void testHeadProjectByIdXml() {
        given()
        .when()
            .head("/projects/" + createdId)
        .then()
            .statusCode(200);
    }

    @Test
    void testPostUpdateProjectXml() {
        String xmlBody = "<project><description>xmlUpdated</description></project>";

        given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body(xmlBody)
        .when()
            .post("/projects/" + createdId)
        .then()
            .statusCode(200)
            .body("project.description", equalTo("xmlUpdated"));
    }

    @Test
    void testPutUpdateProjectXml() {
        String xmlBody = "<project><title>PutTest</title><description>PutXmlUpdate</description><completed>true</completed><active>false</active></project>";

        given()
            .contentType(ContentType.XML)
            .accept(ContentType.XML)
            .body(xmlBody)
        .when()
            .put("/projects/" + createdId)
        .then()
            .statusCode(200)
            .body("project.description", equalTo("PutXmlUpdate"));
    }

    @Test
    void testDeleteProjectXml() {
        given()
        .when()
            .delete("/projects/" + createdId)
        .then()
            .statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    void testGetDeletedProjectXmlShould404() {
        given().when().delete("/projects/" + createdId)
            .then().statusCode(anyOf(is(200), is(204), is(404)));

        given()
            .accept(ContentType.XML)
        .when()
            .get("/projects/" + createdId)
        .then()
            .statusCode(404);
    }
}
