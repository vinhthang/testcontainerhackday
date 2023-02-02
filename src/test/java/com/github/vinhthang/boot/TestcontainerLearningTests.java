package com.github.vinhthang.boot;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.NginxContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class TestcontainerLearningTests {
    @Container
    NginxContainer<?> nginx = new NginxContainer<>("nginx");
    // will be started before and stopped after each test method
    @Container
    private JdbcDatabaseContainer postgresqlContainer = new PostgreSQLContainer("postgres:15.1-alpine")
            .withDatabaseName("foo")
            .withUsername("foo")
            .withPassword("secret").withInitScript("initscript.sql");

    @Test
    void test_that_postgres_is_up() {
        assertTrue(postgresqlContainer.isRunning());
    }

    @Test
    void test_nginx_hello_world() throws IOException, InterruptedException, URISyntaxException {
        var client = HttpClient.newHttpClient();
        var url = nginx.getBaseUrl("http", 80);
        var request = HttpRequest.newBuilder(url.toURI()).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertTrue(response.body().contains("Thank you for using nginx."));    }
}
