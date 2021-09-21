package com.exploring.restassured;

import static io.restassured.RestAssured.when;
import static io.restassured.matcher.RestAssuredMatchers.equalToPath;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import io.restassured.path.json.JsonPath;

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
}
