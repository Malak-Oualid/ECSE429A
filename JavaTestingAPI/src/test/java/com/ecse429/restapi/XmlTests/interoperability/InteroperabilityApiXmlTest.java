package com.ecse429.restapi.XmlTests.interoperability;

import io.restassured.http.ContentType;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;

import com.ecse429.restapi.BaseApiTest;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InteroperabilityApiXmlTest extends BaseApiTest {

    private String todoId;
    private String projectId;
    private String categoryId;

    // Candidate XML paths to tolerate server variants
    private static final String[] TODO_ID_PATHS     = {"todo.id", "todos.todo.id", "item.id"};
    private static final String[] PROJECT_ID_PATHS  = {"project.id", "projects.project.id", "item.id"};
    private static final String[] CATEGORY_ID_PATHS = {"category.id", "categories.category.id", "item.id"};

    // --- helpers (put anywhere in the class) ---
    private static java.util.List<String> getIdsFromResponse(io.restassured.response.Response resp, String... paths) {
        io.restassured.path.xml.XmlPath xp = new io.restassured.path.xml.XmlPath(resp.asString());
        java.util.List<String> out = new java.util.ArrayList<>();
        for (String p : paths) {
            Object v = xp.get(p);
            if (v == null) continue;
            if (v instanceof java.util.List) {
                for (Object o : (java.util.List<?>) v) if (o != null) out.add(String.valueOf(o).trim());
            } else {
                out.add(String.valueOf(v).trim());
            }
        }
        return out;
    }
    
    /** Poll GET a few times in case the relation write is slightly delayed */
    private io.restassured.response.Response getWithRetry(String path, int attempts, long sleepMs) {
        io.restassured.response.Response last = null;
        for (int i = 0; i < attempts; i++) {
            last = given().accept(ContentType.XML).when().get(path).then().statusCode(200).extract().response();
            // if body contains anything non-empty, stop early
            if (last.asString() != null && !last.asString().isBlank()) break;
            try { Thread.sleep(sleepMs); } catch (InterruptedException ignored) {}
        }
        return last;
    }

    private static String xget(XmlPath xp, String... paths) {
        for (String p : paths) {
            String v = xp.getString(p);
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return null;
    }
    private void postRelationJson(String path, String id) {
        given()
            .accept(ContentType.XML)        // ask for XML back
            .contentType(ContentType.JSON)  // but send JSON body
            .body("{\"id\":\"" + id + "\"}")
        .when()
            .post(path)
        .then()
            .statusCode(anyOf(is(200), is(201)));
    }
    
    private static String extractId(Response resp, String... candidates) {
        String xml = resp.asString();
        XmlPath xp = new XmlPath(xml);
        String id = xget(xp, candidates);
        if (id == null) {
            throw new AssertionError("Could not extract id from XML. Tried: " + String.join(", ", candidates) + "\nXML:\n" + xml);
        }
        return id;
    }

    private static void expect200(String path) {
        given().accept(ContentType.XML).when().get(path).then().statusCode(200);
    }

    private void safeDelete(String path) {
        given().when().delete(path).then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    /**
     * Try posting a relation with a specific wrapper (<tag><id>..</id></tag>),
     * and if the server rejects it (404/400/415), retry with <item><id>..</id></item>.
     */
    private void postRelationXml(String path, String resourceTag, String id) {
        String primary = "<" + resourceTag + "><id>" + id + "</id></" + resourceTag + ">";
        Response r = given()
                .contentType(ContentType.XML).accept(ContentType.XML)
                .body(primary).log().all()
            .when()
                .post(path)
            .then()
                .log().all()
                .extract().response();

        int sc = r.statusCode();
        if (sc == 200 || sc == 201) return;

        // Retry with <item>
        String fallback = "<item><id>" + id + "</id></item>";
        given()
            .contentType(ContentType.XML).accept(ContentType.XML)
            .body(fallback).log().all()
        .when()
            .post(path)
        .then()
            .log().all()
            .statusCode(anyOf(is(200), is(201)));
    }

    @BeforeAll
    void createSharedEntities() {
        // create todo
        Response todoResp = given()
            .contentType(ContentType.XML).accept(ContentType.XML)
            .body("<todo><title>interopTodo</title><doneStatus>false</doneStatus><description>interop todo</description></todo>")
            .log().ifValidationFails()
        .when()
            .post("/todos")
        .then()
            .log().ifValidationFails()
            .statusCode(anyOf(is(200), is(201)))
            .extract().response();
        todoId = extractId(todoResp, TODO_ID_PATHS);

        // create project
        Response projResp = given()
            .contentType(ContentType.XML).accept(ContentType.XML)
            .body("<project><title>interopProject</title><completed>false</completed><active>true</active><description>interop project</description></project>")
            .log().ifValidationFails()
        .when()
            .post("/projects")
        .then()
            .log().ifValidationFails()
            .statusCode(anyOf(is(200), is(201)))
            .extract().response();
        projectId = extractId(projResp, PROJECT_ID_PATHS);

        // create category
        Response catResp = given()
            .contentType(ContentType.XML).accept(ContentType.XML)
            .body("<category><title>interopCategory</title><description>interop category</description></category>")
            .log().ifValidationFails()
        .when()
            .post("/categories")
        .then()
            .log().ifValidationFails()
            .statusCode(anyOf(is(200), is(201)))
            .extract().response();
        categoryId = extractId(catResp, CATEGORY_ID_PATHS);

        // sanity checks
        expect200("/todos/" + todoId);
        expect200("/projects/" + projectId);
        expect200("/categories/" + categoryId);
    }

    @AfterAll
    void cleanupAll() {
        // unlink relations (both sides), tolerate 404s
        safeDelete(String.format("/todos/%s/categories/%s", todoId, categoryId));
        safeDelete(String.format("/projects/%s/categories/%s", projectId, categoryId));
        safeDelete(String.format("/projects/%s/tasks/%s", projectId, todoId));
        safeDelete(String.format("/categories/%s/todos/%s", categoryId, todoId));
        safeDelete(String.format("/categories/%s/projects/%s", categoryId, projectId));
        safeDelete(String.format("/todos/%s/tasksof/%s", todoId, projectId));

        // delete entities
        safeDelete("/todos/" + todoId);
        safeDelete("/projects/" + projectId);
        safeDelete("/categories/" + categoryId);
    }

    @Test
    void linkTodoToProjectViaTasksAndVerify() {
        postRelationJson("/projects/" + projectId + "/tasks", todoId);

        // Verify tasks under project (handle multiple XML shapes)
        var projTasksResp = getWithRetry("/projects/" + projectId + "/tasks", 3, 150);
        var taskIds = getIdsFromResponse(
            projTasksResp,
            // common shapes:
            "projects.todos.todo.id",
            "todos.todo.id",
            "projects.project.todos.todo.id",
            // looser fallbacks some builds use:
            "projects.todos.id",
            "projects.project.tasks.todo.id"
        );
        Assertions.assertTrue(taskIds.contains(todoId),
            "Expected todoId " + todoId + " in project tasks, got: " + taskIds + "\nXML:\n" + projTasksResp.asString());

        // Verify project appears in todo tasksof (multiple shapes + single/list)
        var todoTasksofResp = getWithRetry("/todos/" + todoId + "/tasksof", 3, 150);
        var projectIds = getIdsFromResponse(
            todoTasksofResp,
            "todos.projects.project.id",
            "projects.project.id",
            "todos.project.id"
        );
        Assertions.assertTrue(projectIds.contains(projectId),
            "Expected projectId " + projectId + " in todo tasksof, got: " + projectIds + "\nXML:\n" + todoTasksofResp.asString());
    }


    @Test
    void linkProjectFromTodoSideAndVerify() {
        // Link from todo -> project
        postRelationJson("/todos/" + todoId + "/tasksof", projectId);
    
        // --- Verify from /todos/:id/tasksof ---
        var tasksofResp = getWithRetry("/todos/" + todoId + "/tasksof", 4, 150);
        var projectIds = getIdsFromResponse(
            tasksofResp,
            // common shapes:
            "todos.projects.project.id",
            "projects.project.id",
            // looser fallbacks some builds use:
            "projects.id",          // single project
            "project.id"            // very minimal
        );
        Assertions.assertTrue(
            projectIds.contains(projectId),
            "Expected projectId " + projectId + " in /todos/" + todoId + "/tasksof.\n"
            + "Found: " + projectIds + "\nXML:\n" + tasksofResp.asString()
        );
    
        // --- Verify from /projects/:id/tasks ---
        var projTasksResp = getWithRetry("/projects/" + projectId + "/tasks", 4, 150);
        var todoIds = getIdsFromResponse(
            projTasksResp,
            // common shapes:
            "projects.todos.todo.id",
            "todos.todo.id",
            // fallbacks:
            "projects.todos.id",    // some builds flatten 'todo'
            "todo.id"
        );
        Assertions.assertTrue(
            todoIds.contains(todoId),
            "Expected todoId " + todoId + " in /projects/" + projectId + "/tasks.\n"
            + "Found: " + todoIds + "\nXML:\n" + projTasksResp.asString()
        );
    }
    

    @Test
    void linkTodoToCategoryAndProjectToCategoryAndVerify() {
        // Link from todo side (JSON body)
        postRelationJson("/todos/" + todoId + "/categories", categoryId);
    
        // Verify via GET /todos/:id/categories (handle single vs list + wrapper variants)
        var todoCatsResp = getWithRetry("/todos/" + todoId + "/categories", 4, 150);
        var catIdsFromTodo = getIdsFromResponse(
            todoCatsResp,
            "todos.categories.category.id",  // common
            "categories.category.id",        // sometimes no top-level 'todos'
            "category.id"                    // very compact response
        );
        Assertions.assertTrue(
            catIdsFromTodo.contains(categoryId),
            "Expected categoryId " + categoryId + " under /todos/" + todoId + "/categories.\n"
            + "Found: " + catIdsFromTodo + "\nXML:\n" + todoCatsResp.asString()
        );
    
        // Link from project side — you were using postRelationXml; switch to JSON here too
        postRelationJson("/projects/" + projectId + "/categories", categoryId);
    
        // Verify via GET /projects/:id/categories (same flexible parsing)
        var projCatsResp = getWithRetry("/projects/" + projectId + "/categories", 4, 150);
        var catIdsFromProject = getIdsFromResponse(
            projCatsResp,
            "projects.categories.category.id", // common
            "categories.category.id",
            "category.id"
        );
        Assertions.assertTrue(
            catIdsFromProject.contains(categoryId),
            "Expected categoryId " + categoryId + " under /projects/" + projectId + "/categories.\n"
            + "Found: " + catIdsFromProject + "\nXML:\n" + projCatsResp.asString()
        );
    }
    
    @Test
    void categorySideMirrors_TodosAndProjects() {
        postRelationJson("/categories/" + categoryId + "/todos", todoId);
        postRelationJson("/categories/" + categoryId + "/projects", projectId);

        // Accept both single-value and list responses
        given().accept(ContentType.XML)
        .when().get("/categories/" + categoryId + "/todos")
        .then().statusCode(200)
            .body("todos.todo.id", anyOf(equalTo(todoId), hasItem(todoId)));

        given().accept(ContentType.XML)
        .when().get("/categories/" + categoryId + "/projects")
        .then().statusCode(200)
            .body("projects.project.id", anyOf(equalTo(projectId), hasItem(projectId)));
    }

    @Test
    void headEndpoints_Smoke() {
        // Collections
        given().when().head("/todos").then().statusCode(200);
        given().when().head("/projects").then().statusCode(200);
        given().when().head("/categories").then().statusCode(200);

        // Instances
        given().when().head("/todos/" + todoId).then().statusCode(200);
        given().when().head("/projects/" + projectId).then().statusCode(200);
        given().when().head("/categories/" + categoryId).then().statusCode(200);

        // Relationships
        given().when().head("/projects/" + projectId + "/tasks").then().statusCode(200);
        given().when().head("/todos/" + todoId + "/tasksof").then().statusCode(200);
        given().when().head("/todos/" + todoId + "/categories").then().statusCode(200);
        given().when().head("/projects/" + projectId + "/categories").then().statusCode(200);
        given().when().head("/categories/" + categoryId + "/todos").then().statusCode(200);
        given().when().head("/categories/" + categoryId + "/projects").then().statusCode(200);
    }

    @Test
    void collectionFilters_Smoke() {
        String doneTodoId = extractId(
            given().contentType(ContentType.XML).accept(ContentType.XML)
                .body("<todo><title>doneOne</title><doneStatus>true</doneStatus></todo>")
            .when().post("/todos")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response(),
            TODO_ID_PATHS
        );

        given().accept(ContentType.XML)
        .when().get("/todos?doneStatus=true")
        .then().statusCode(200)
               .body("todos.todo.id", hasItem(doneTodoId));

        given().accept(ContentType.XML)
        .when().get("/projects?active=true")
        .then().statusCode(200);

        safeDelete("/todos/" + doneTodoId);
    }

    @Test
    void amendEndpoints_PutAndPost() {
        Response resp = given()
            .contentType(ContentType.XML).accept(ContentType.XML)
            .body("<todo><title>interopTodoUpdated</title><doneStatus>true</doneStatus></todo>")
            .log().ifValidationFails()
        .when()
            .put("/todos/" + todoId)
        .then()
            .log().ifValidationFails()
            .statusCode(anyOf(is(200), is(201)))
            .extract().response();

        XmlPath xp = new XmlPath(resp.asString());
        // Use precise paths to avoid concatenation like "truenull17interopTodoUpdated"
        String title = xget(xp, "todo.title", "todos.todo[0].title");
        Assertions.assertEquals("interopTodoUpdated", title, "title not updated");

        String ds = xget(xp, "todo.doneStatus", "todos.todo[0].doneStatus");
        Assertions.assertNotNull(ds, "doneStatus not found");
        Assertions.assertTrue(Boolean.parseBoolean(ds), "doneStatus should be true but was: " + ds);
    }

    @Test
    void deleteFromMirrorSides() {
        safeDelete("/categories/" + categoryId + "/todos/" + todoId);
        safeDelete("/categories/" + categoryId + "/projects/" + projectId);
        safeDelete("/todos/" + todoId + "/tasksof/" + projectId);
    }
}
