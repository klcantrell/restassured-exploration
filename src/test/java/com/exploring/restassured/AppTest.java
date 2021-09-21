package com.exploring.restassured;

import com.google.gson.Gson;
import io.restassured.path.json.JsonPath;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static io.restassured.RestAssured.when;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.matcher.RestAssuredMatchers.equalToPath;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static java.net.http.HttpRequest.BodyPublishers;
import static java.net.http.HttpResponse.BodyHandlers;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class AppTest {
    @Test
    public void shouldRetrieveTheFirstTodo() {
        when()
                .get("https://jsonplaceholder.typicode.com/todos/1")
                .then()
                .statusCode(200)
                .body(
                        "id", equalTo(1),
                        "title", equalTo("delectus aut autem"),
                        "completed", equalTo(false)
                )
                .and()
                .body(
                        "userId", equalToPath("id")
                );
    }

    @Test
    public void shouldRetrieveTenTodos() {
        var expectedIds = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        var expectedTitles = Arrays.asList(
                "delectus aut autem",
                "quis ut nam facilis et officia qui",
                "fugiat veniam minus",
                "et porro tempora",
                "laboriosam mollitia et enim quasi adipisci quia provident illum",
                "qui ullam ratione quibusdam voluptatem quia omnis",
                "illo expedita consequatur quia in",
                "quo adipisci enim quam ut ab",
                "molestiae perspiciatis ipsa",
                "illo est ratione doloremque quia maiores aut"
        );

        JsonPath response = when()
                .get("https://jsonplaceholder.typicode.com/todos?_start=0&_end=10")
                .then()
                .statusCode(200)
                .body(
                        matchesJsonSchemaInClasspath("todo-schema.json")
                )
                .contentType(JSON)
                .extract()
                .response()
                .jsonPath();

        List<Integer> ids = response.getList("id");
        List<String> titles = response.getList("title");
        assertThat(ids, equalTo(expectedIds));
        assertThat(titles, equalTo(expectedTitles));
    }

    @Test
    public void shouldRetrieveTenTodosAsObjects() {
        var expectedTodos = Arrays.asList(
                new Todo(1, 1, "delectus aut autem", false),
                new Todo(1, 2, "quis ut nam facilis et officia qui", false),
                new Todo(1, 3, "fugiat veniam minus", false),
                new Todo(1, 4, "et porro tempora", true),
                new Todo(1, 5, "laboriosam mollitia et enim quasi adipisci quia provident illum", false),
                new Todo(1, 6, "qui ullam ratione quibusdam voluptatem quia omnis", false),
                new Todo(1, 7, "illo expedita consequatur quia in", false),
                new Todo(1, 8, "quo adipisci enim quam ut ab", true),
                new Todo(1, 9, "molestiae perspiciatis ipsa", false),
                new Todo(1, 10, "illo est ratione doloremque quia maiores aut", true)
        );
        List<Todo> todos = Arrays.asList(when()
                .get("https://jsonplaceholder.typicode.com/todos?_start=0&_end=10")
                .then()
                .statusCode(200)
                .body(
                        matchesJsonSchemaInClasspath("todo-schema.json")
                )
                .contentType(JSON)
                .extract()
                .response()
                .getBody()
                .as(Todo[].class)
        );

        assertThat(todos, equalTo(expectedTodos));
    }

    @Test
    public void shouldRetrieveOauthToken() throws IOException, InterruptedException {
        System.out.println(getOauthToken());
    }

    private String getOauthToken() throws IOException, InterruptedException {
        var properties = getProperties("test.properties");
        var oauthTokenEndpoint = properties.getProperty("oauth_token_endpoint");

        var clientId = properties.getProperty("client_id");
        var clientSecret = properties.getProperty("client_secret");

        var httpClient = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .headers(
                        "Accept", "application/json",
                        "Content-Type", "application/x-www-form-urlencoded"
                )
                .uri(URI.create(oauthTokenEndpoint))
                .POST(BodyPublishers.ofString(
                        "client_id=" + clientId
                                + "&client_secret=" + clientSecret
                                + "&grant_type=client_credentials"
                                + "&scope=rest_assured"
                ))
                .build();
        var response = httpClient.send(request, BodyHandlers.ofString());
        var responseBody = new Gson().fromJson(response.body(), OauthTokenResponse.class);
        return response.body();
    }

    private Properties getProperties(String fileName) throws IOException {
        var properties = new Properties();
        InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
        properties.load(in);
        return properties;
    }
}
