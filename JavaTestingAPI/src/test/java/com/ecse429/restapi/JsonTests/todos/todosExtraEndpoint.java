package com.ecse429.restapi.JsonTests.todos;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.Random.class)
public class todosExtraEndpoint extends com.ecse429.restapi.BaseApiTest {

    @Test
    @Order(1)
    void testCreateToDoWithAllFields() {
        String idString = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"title\":\"Full ToDo\",\"doneStatus\":true,\"description\":\"A full todo\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .body("title", equalTo("Full ToDo"))
                .body("doneStatus", equalTo("true"))
                .body("description", equalTo("A full todo"))
                .extract()
                .path("id");
        int id = Integer.parseInt(idString);
        // Clean up
        given().when().delete("/todos/" + id).then().statusCode(200);
    }

    @Test
    @Order(2)
    void testFilterTodosByTitle() {
        // Create a unique todo
        String uniqueTitle = "UniqueTitle123";
        String idString = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"title\":\"" + uniqueTitle + "\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
        // Filter by title
        given()
                .accept(ContentType.JSON)
                .queryParam("title", uniqueTitle)
                .when()
                .get("/todos")
                .then()
                .statusCode(200)
                .body("todos[0].title", equalTo(uniqueTitle));
        // Clean up
        given().when().delete("/todos/" + idString).then().statusCode(200);
    }

    @Test
    @Order(3)
    void testGetCategoriesOfToDo() {
        // Create a todo
        String idString = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"title\":\"CatTest\"}")
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
        // Get categories
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/todos/" + idString + "/categories")
                .then()
                .statusCode(200)
                .body("categories.size()", equalTo(0));
        // Clean up
        given().when().delete("/todos/" + idString).then().statusCode(200);
    }

    @Test
    @Order(4)
    void testDeleteNonExistentToDo() {
        given()
                .when()
                .delete("/todos/999999")
                .then()
                .statusCode(anyOf(is(404), is(200)));
    }

    @Test
    @Order(5)
    void testPutUpdateToDoAllFields() {
                // Create a todo and extract the ID
                String idString = given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body("{\"title\":\"PutAllTest\"}")
                .when()
                        .post("/todos")
                .then()
                        .statusCode(201)
                        .extract()
                        .path("id").toString();

                // Update the todo with all fields (doneStatus as boolean)
                given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body("{\"title\":\"UpdatedTitle\",\"doneStatus\":true,\"description\":\"UpdatedDesc\"}")
                .when()
                        .put("/todos/" + idString)
                .then()
                        .statusCode(200)
                        .body("title", equalTo("UpdatedTitle"))
                        .body("doneStatus", equalTo("true"))
                        .body("description", equalTo("UpdatedDesc"));

                // Clean up
                given().when().delete("/todos/" + idString).then().statusCode(200);
    }
}
