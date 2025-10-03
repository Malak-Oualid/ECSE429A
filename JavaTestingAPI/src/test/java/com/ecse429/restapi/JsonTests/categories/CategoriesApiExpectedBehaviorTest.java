package com.ecse429.restapi.JsonTests.categories;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import com.ecse429.restapi.BaseApiTest;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Categories API - Expected Behavior
 * Tests the /categories endpoint according to the API documentation.
 * These tests represent what SHOULD happen based on the documented API contract.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoriesApiExpectedBehaviorTest extends BaseApiTest {

    private static String createdCategoryId;

    @BeforeEach
    public void setup() {
        // Ensure clean state before each test
        createdCategoryId = null;
    }

    @AfterEach
    public void cleanup() {
        // Clean up any categories created during tests
        if (createdCategoryId != null) {
            try {
                delete("/categories/" + createdCategoryId);
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    // ==================== GET /categories ====================

    @Test
    @Order(1)
    @DisplayName("GET /categories should return 200 and JSON response")
    public void testGetAllCategories() {
        given()
            .accept("application/json")
        .when()
            .get("/categories")
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"))
            .body("categories", notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("HEAD /categories should return 200")
    public void testHeadCategories() {
        when()
            .head("/categories")
        .then()
            .statusCode(200);
    }

    // ==================== POST /categories ====================

    @Test
    @Order(3)
    @DisplayName("POST /categories should create category with title='67' and return 201")
    public void testCreateCategoryWithTitle67() {
        String requestBody = "{ \"title\": \"67\" }";

        Response response = given()
            .contentType("application/json")
            .body(requestBody)
        .when()
            .post("/categories")
        .then()
            .statusCode(201)
            .contentType(containsString("application/json"))
            .body("title", equalTo("67"))
            .body("id", notNullValue())
        .extract()
            .response();

        // Store ID for cleanup
        createdCategoryId = response.jsonPath().getString("id");
        assertNotNull(createdCategoryId, "Created category should have an ID");
    }

    @Test
    @Order(4)
    @DisplayName("POST /categories should create category with all fields")
    public void testCreateCategoryWithAllFields() {
        String requestBody = "{ \"title\": \"Work\", \"description\": \"Work related tasks\" }";

        Response response = given()
            .contentType("application/json")
            .body(requestBody)
        .when()
            .post("/categories")
        .then()
            .statusCode(201)
            .contentType(containsString("application/json"))
            .body("title", equalTo("Work"))
            .body("description", equalTo("Work related tasks"))
            .body("id", notNullValue())
        .extract()
            .response();

        createdCategoryId = response.jsonPath().getString("id");
    }

    // ==================== GET /categories/{id} ====================

    @Test
    @Order(5)
    @DisplayName("GET /categories/{id} should return 200 for existing category")
    public void testGetExistingCategory() {
        // First create a category
        String requestBody = "{ \"title\": \"TestCategory\" }";

        Response createResponse = given()
            .contentType("application/json")
            .body(requestBody)
        .when()
            .post("/categories");

        createdCategoryId = createResponse.jsonPath().getString("id");

        // Then retrieve it
        given()
            .accept("application/json")
        .when()
            .get("/categories/" + createdCategoryId)
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"))
            .body("categories[0].id", equalTo(createdCategoryId))
            .body("categories[0].title", equalTo("TestCategory"));
    }

    @Test
    @Order(6)
    @DisplayName("GET /categories/{id} should return 404 for nonexistent category")
    public void testGetNonexistentCategory() {
        String invalidId = "99999";

        given()
            .accept("application/json")
        .when()
            .get("/categories/" + invalidId)
        .then()
            .statusCode(404)
            .contentType(containsString("application/json"))
            .body("errorMessages", hasItem(containsString("Could not find")));
    }

    // ==================== POST /categories/{id} ====================

    @Test
    @Order(7)
    @DisplayName("POST /categories/{id} should update category and return 200")
    public void testUpdateCategoryWithPost() {
        // First create a category
        String createBody = "{ \"title\": \"Original\" }";

        Response createResponse = given()
            .contentType("application/json")
            .body(createBody)
        .when()
            .post("/categories");

        createdCategoryId = createResponse.jsonPath().getString("id");

        // Then update it
        String updateBody = "{ \"title\": \"Updated\", \"description\": \"New description\" }";

        given()
            .contentType("application/json")
            .body(updateBody)
        .when()
            .post("/categories/" + createdCategoryId)
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"))
            .body("title", equalTo("Updated"))
            .body("description", equalTo("New description"));
    }

    @Test
    @Order(8)
    @DisplayName("POST /categories/{invalidId} should return error with 'No such category'")
    public void testUpdateNonexistentCategoryWithPost() {
        String invalidId = "99999";
        String updateBody = "{ \"title\": \"ShouldFail\" }";

        given()
            .contentType("application/json")
            .body(updateBody)
        .when()
            .post("/categories/" + invalidId)
        .then()
            .statusCode(404)
            .contentType(containsString("application/json"))
            .body("errorMessages", hasItem(containsString("No such category")));
    }

    // ==================== PUT /categories/{id} ====================

    @Test
    @Order(9)
    @DisplayName("PUT /categories/{id} should update description and return 200")
    public void testUpdateCategoryWithPut() {
        // First create a category
        String createBody = "{ \"title\": \"ToPutUpdate\" }";

        Response createResponse = given()
            .contentType("application/json")
            .body(createBody)
        .when()
            .post("/categories");

        createdCategoryId = createResponse.jsonPath().getString("id");

        // Then update it with PUT
        String updateBody = "{ \"description\": \"Updated via PUT\" }";

        given()
            .contentType("application/json")
            .body(updateBody)
        .when()
            .put("/categories/" + createdCategoryId)
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"))
            .body("description", equalTo("Updated via PUT"));
    }

    @Test
    @Order(10)
    @DisplayName("PUT /categories/{invalidId} should return 404 with error message")
    public void testUpdateNonexistentCategoryWithPut() {
        String invalidId = "99999";
        String updateBody = "{ \"description\": \"ShouldFail\" }";

        given()
            .contentType("application/json")
            .body(updateBody)
        .when()
            .put("/categories/" + invalidId)
        .then()
            .statusCode(404)
            .contentType(containsString("application/json"))
            .body("errorMessages", notNullValue())
            .body("errorMessages", hasSize(greaterThan(0)));
    }

    // ==================== DELETE /categories/{id} ====================

    @Test
    @Order(11)
    @DisplayName("DELETE /categories/{id} should delete category and return 200")
    public void testDeleteCategory() {
        // First create a category
        String createBody = "{ \"title\": \"ToDelete\" }";

        Response createResponse = given()
            .contentType("application/json")
            .body(createBody)
        .when()
            .post("/categories");

        String categoryId = createResponse.jsonPath().getString("id");

        // Then delete it
        when()
            .delete("/categories/" + categoryId)
        .then()
            .statusCode(200);

        // Verify it's deleted by trying to get it
        given()
            .accept("application/json")
        .when()
            .get("/categories/" + categoryId)
        .then()
            .statusCode(404);

        // Don't set createdCategoryId since we already deleted it
    }

    @Test
    @Order(12)
    @DisplayName("DELETE /categories/{invalidId} should return error with 'Could not find'")
    public void testDeleteNonexistentCategory() {
        String invalidId = "99999";

        given()
            .accept("application/json")
        .when()
            .delete("/categories/" + invalidId)
        .then()
            .statusCode(404)
            .contentType(containsString("application/json"))
            .body("errorMessages", hasItem(containsString("Could not find")));
    }

    // ==================== Edge Cases ====================

    @Test
    @Order(13)
    @DisplayName("POST /categories with empty title should handle gracefully")
    public void testCreateCategoryWithEmptyTitle() {
        String requestBody = "{ \"title\": \"\" }";

        Response response = given()
            .contentType("application/json")
            .body(requestBody)
        .when()
            .post("/categories");

        // API might accept empty title or reject it
        int statusCode = response.getStatusCode();
        assertTrue(statusCode == 201 || statusCode == 400,
                "Status code should be 201 (accepted) or 400 (rejected)");

        if (statusCode == 201) {
            createdCategoryId = response.jsonPath().getString("id");
        }
    }

    @Test
    @Order(14)
    @DisplayName("POST /categories with no body should return 400")
    public void testCreateCategoryWithNoBody() {
        given()
            .contentType("application/json")
        .when()
            .post("/categories")
        .then()
            .statusCode(400)
            .contentType(containsString("application/json"))
            .body("errorMessages", notNullValue());
    }

    @Test
    @Order(15)
    @DisplayName("GET /categories with XML accept header should return XML")
    public void testGetCategoriesXml() {
        given()
            .accept("application/xml")
        .when()
            .get("/categories")
        .then()
            .statusCode(200)
            .contentType(containsString("application/xml"));
    }
}
