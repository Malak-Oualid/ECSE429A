package com.ecse429.restapi.JsonTests.undocumentedEndpoints;

import com.ecse429.restapi.BaseApiTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.anyOf;

public class GetCategoriesFromTodosUndocTest extends BaseApiTest {
    @Test
    void getCategoriesFromTodosReturnsCategoriesList() {
        String unique = String.valueOf(System.currentTimeMillis());
        // Create a todo
        String todoId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"undocTodo" + unique + "\",\"doneStatus\":false}")
        .when().post("/todos")
        .then().statusCode(anyOf(is(200), is(201))).extract().path("id");
        Assertions.assertNotNull(todoId);

        // Create a category
        String categoryId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"undocCategory" + unique + "\",\"description\":\"desc\"}")
        .when().post("/categories")
        .then().statusCode(anyOf(is(200), is(201))).extract().path("id");
        Assertions.assertNotNull(categoryId);

        // Link todo to category
        given()
            .contentType(ContentType.JSON)
            .body(String.format("{\"id\":\"%s\"}", categoryId))
        .when().post("/todos/" + todoId + "/categories")
        .then().statusCode(anyOf(is(200), is(201)));

        // Now /todos/categories should return our category
        var resp = given()
            .accept(ContentType.JSON)
        .when().get("/todos/categories")
        .then().statusCode(200)
            .contentType(ContentType.JSON)
            .extract().response();
        Assertions.assertNotNull(resp);

        java.util.List<java.util.Map<String, Object>> categories = resp.jsonPath().getList("categories");
        boolean found = false;
        for (java.util.Map<String, Object> cat : categories) {
            if (categoryId.equals(String.valueOf(cat.get("id"))) &&
                ("undocCategory" + unique).equals(cat.get("title")) &&
                "desc".equals(cat.get("description"))) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Expected to find the created category in /todos/categories response");

        // Cleanup
        try { given().when().delete("/todos/" + todoId); } catch (Exception ignored) {}
        try { given().when().delete("/categories/" + categoryId); } catch (Exception ignored) {}
    }
}
