package org.acme;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class GreetingResourceTest {
    @Test
    void testStatelessSessionCrudFlow() {
        Response createResponse = given()
                .contentType("text/plain")
                .body("Book")
                .when().post("/gifts")
                .then()
                .statusCode(201)
                .body("name", is("Book"))
                .extract()
                .response();

        Long createdId = createResponse.jsonPath().getLong("id");

        given()
                .when().get("/gifts")
                .then()
                .statusCode(200)
                .body("name", hasItems("Sample gift", "Book"));

        given()
                .when().delete("/gifts/" + createdId)
                .then()
                .statusCode(204);

        given()
                .when().get("/gifts")
                .then()
                .statusCode(200)
                .body("name", hasItem("Sample gift"))
                .body("name", not(hasItem("Book")));
    }
}