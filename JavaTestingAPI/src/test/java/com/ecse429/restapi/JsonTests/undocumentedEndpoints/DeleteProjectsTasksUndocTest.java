package com.ecse429.restapi.JsonTests.undocumentedEndpoints;

import com.ecse429.restapi.BaseApiTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;

public class DeleteProjectsTasksUndocTest extends BaseApiTest {
    @Test
    void deleteProjectsTasksRemovesProjectTaskRelations() throws InterruptedException {
        String unique = String.valueOf(System.currentTimeMillis());
        String todoId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"undocTodo" + unique + "\",\"doneStatus\":false}")
        .when().post("/todos")
        .then().statusCode(anyOf(is(200), is(201))).extract().path("id");
        Assertions.assertNotNull(todoId);
        String projectId = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"title\":\"undocProject" + unique + "\",\"completed\":false,\"active\":true}")
        .when().post("/projects")
        .then().statusCode(anyOf(is(200), is(201))).extract().path("id");
        Assertions.assertNotNull(projectId);
        given()
            .contentType(ContentType.JSON)
            .body(String.format("{\"id\":\"%s\"}", todoId))
        .when().post("/projects/" + projectId + "/tasks")
        .then().statusCode(anyOf(is(200), is(201)));
        given().accept(ContentType.JSON)
        .when().get("/projects/" + projectId + "/tasks")
        .then().statusCode(200).body("todos.id", hasItem(todoId));
        given().when().delete("/projects/tasks");
        Thread.sleep(500);
        var ids = given().accept(ContentType.JSON)
            .when().get("/projects/" + projectId + "/tasks")
            .then().statusCode(200).extract().path("todos.id");
        Assertions.assertTrue(ids == null || !ids.toString().contains(todoId));
        try { given().when().delete("/todos/" + todoId); } catch (Exception ignored) {}
        try { given().when().delete("/projects/" + projectId); } catch (Exception ignored) {}
    }
}
