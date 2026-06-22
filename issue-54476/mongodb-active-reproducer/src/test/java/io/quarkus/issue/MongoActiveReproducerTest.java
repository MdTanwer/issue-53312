package io.quarkus.issue;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Reproducer for <a href="https://github.com/quarkusio/quarkus/issues/54476">#54476</a>.
 * <p>
 * With {@code quarkus.mongodb.active=false}, startup should succeed even when the
 * connection string contains URL-unsafe template placeholders ({@code %s}).
 * <p>
 * Currently fails with:
 * {@code java.lang.IllegalArgumentException: URLDecoder: Illegal hex characters in escape (%) pattern}
 */
@QuarkusTest
class MongoActiveReproducerTest {

    @Test
    void applicationStartsWithInactiveMongoClientAndTemplateConnectionString() {
        given()
                .when().get("/hello")
                .then()
                .statusCode(200)
                .body(equalTo("Hello from mongodb-active-reproducer"));

        given()
                .when().get("/api/mongo/template")
                .then()
                .statusCode(200)
                .body("active", is(false))
                .body("connectionStringTemplate", equalTo("mongodb://%s:%s@x.y.z.w:27017/db"));
    }
}
