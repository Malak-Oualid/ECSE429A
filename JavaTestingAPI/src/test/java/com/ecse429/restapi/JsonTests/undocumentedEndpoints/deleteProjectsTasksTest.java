package com.ecse429.restapi.JsonTests.undocumentedEndpoints;

import com.ecse429.restapi.BaseApiTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.CoreMatchers.is;


// Undocumented endpoint test: DELETE /projects/tasks
// Assumes the server is started externally with default configuration.

public class deleteProjectsTasksTest extends BaseApiTest {

    @Test
    void deleteProjectsTasksShouldReturn200or204or404() {
        given()
            .accept(ContentType.JSON)
        .when()
            .delete("/projects/tasks")
        .then()
            .statusCode(anyOf(is(200), is(204), is(404)));
    }
}
