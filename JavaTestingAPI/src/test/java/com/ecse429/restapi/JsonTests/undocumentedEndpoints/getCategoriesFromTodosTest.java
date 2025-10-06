package com.ecse429.restapi.JsonTests.undocumentedEndpoints;

import com.ecse429.restapi.BaseApiTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;//


// Undocumented endpoint test: GET /todos/categories
// This test restarts the server before running to ensure default data is present.
public class getCategoriesFromTodosTest extends BaseApiTest {

	private Process serverProcess;

	@BeforeEach
	void restartServer() throws IOException, InterruptedException {
		// Try to shut down any running server first (ignore failures)
		try {
			given().when().get("/shutdown");
		} catch (Exception ignored) {
		}

		// Locate jar: assume run from JavaTestingAPI module, jar at parent
		File moduleDir = new File(System.getProperty("user.dir"));
		File jarFile = new File(moduleDir.getParentFile(), "runTodoManagerRestAPI-1.5.5.jar");
		if (!jarFile.exists()) {
			throw new IllegalStateException("Could not find runTodoManagerRestAPI-1.5.5.jar at " + jarFile.getAbsolutePath());
		}

		ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarFile.getAbsolutePath());
		pb.directory(moduleDir.getParentFile());
		pb.redirectErrorStream(true);
		serverProcess = pb.start();

		// Wait until server is responsive on /todos
		Instant end = Instant.now().plus(Duration.ofSeconds(30));
		boolean ready = false;
		while (Instant.now().isBefore(end)) {
				try {
				given()
					.when()
					.get("/todos")
				.then()
					.extract()
					.statusCode();
				ready = true;
				break;
			} catch (Exception e) {
				Thread.sleep(200);
			}
		}

		if (!ready) {
			if (serverProcess.isAlive()) serverProcess.destroyForcibly();
			throw new IllegalStateException("Server did not become ready within timeout");
		}
	}

	@AfterEach
	void stopServer() {
		try {
			given().when().get("/shutdown");
		} catch (Exception ignored) {
		}
		if (serverProcess != null && serverProcess.isAlive()) {
			serverProcess.destroyForcibly();
		}
	}

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
