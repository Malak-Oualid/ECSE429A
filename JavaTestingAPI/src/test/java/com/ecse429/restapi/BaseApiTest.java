package com.ecse429.restapi;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base test class for all API tests.
 * Sets up RestAssured configuration for testing the Todo Manager REST API.
 */
public abstract class BaseApiTest {

    protected static final String BASE_URI = "http://localhost";
    protected static final int PORT = 4567;
    protected static final String BASE_PATH = "";

    /**
     * Configure RestAssured before all tests.
     * Sets the base URI and port for the REST API.
     */
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = PORT;
        RestAssured.basePath = BASE_PATH;
        
        // Enable logging for debugging (optional)
        // RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
