package io.quarkus.issue;

import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Sample API reflecting the real-world scenario from
 * <a href="https://github.com/quarkusio/quarkus/issues/54476">#54476</a>:
 * the default Mongo client is inactive, and a template connection string is kept
 * in config for manual client construction at runtime.
 */
@Path("/api/mongo")
public class MongoTemplateResource {

    @ConfigProperty(name = "quarkus.mongodb.active")
    boolean mongoActive;

    @ConfigProperty(name = "quarkus.mongodb.connection-string")
    Optional<String> connectionString;

    @GET
    @Path("/template")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> template() {
        return Map.of(
                "active", mongoActive,
                "connectionStringTemplate", connectionString.orElse(""),
                "note", "Client is built manually at runtime from the template when credentials are available");
    }
}
