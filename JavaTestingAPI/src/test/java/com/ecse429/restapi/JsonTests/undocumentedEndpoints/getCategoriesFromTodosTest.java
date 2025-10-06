package com.ecse429.restapi.JsonTests.undocumentedEndpoints;

import com.ecse429.restapi.BaseApiTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;


//Assumes the server is started externally with default configuration.

public class getCategoriesFromTodosTest extends BaseApiTest {

	@Test
	void getCategoriesFromTodosReturnsCategoriesList() {
		given()
			.accept(ContentType.JSON)
		.when()
			.get("/todos/categories")
		.then()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("categories", hasSize(1))
			.body("categories[0].id", equalTo("1"))
			.body("categories[0].title", equalTo("Office"))
			.body("categories[0].description", equalTo(""));
	}
}
