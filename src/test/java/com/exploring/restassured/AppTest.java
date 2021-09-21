package com.exploring.restassured;

import com.google.gson.Gson;
import io.restassured.path.json.JsonPath;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static io.restassured.RestAssured.when;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.matcher.RestAssuredMatchers.equalToPath;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
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
        List<Integer> expectedIds = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<String> expectedTitles = Arrays.asList(
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
        List<Todo> expectedTodos = Arrays.asList(
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
        Properties properties = getProperties("test.properties");
        String oauthTokenEndpoint = properties.getProperty("oauth_token_endpoint");

        String clientId = properties.getProperty("client_id");
        String clientSecret = properties.getProperty("client_secret");

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(oauthTokenEndpoint);
        request.addHeader("Accept", "application/json");
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        List<NameValuePair> params = Arrays.asList(
                new BasicNameValuePair("client_id", clientId),
                new BasicNameValuePair("client_secret", clientSecret),
                new BasicNameValuePair("grant_type", "client_credentials"),
                new BasicNameValuePair("scope", "rest_assured")
        );
        request.setEntity(new UrlEncodedFormEntity(params));
        CloseableHttpResponse response = httpClient.execute(request);
        OauthTokenResponse responseBody = new Gson().fromJson(
                EntityUtils.toString(response.getEntity()), OauthTokenResponse.class
        );
        response.close();
        httpClient.close();
        return responseBody.access_token;
    }

    private Properties getProperties(String fileName) throws IOException {
        Properties properties = new Properties();
        InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
        properties.load(in);
        return properties;
    }
}
