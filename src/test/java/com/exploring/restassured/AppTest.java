package com.exploring.restassured;

import io.restassured.path.json.JsonPath;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.when;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.matcher.RestAssuredMatchers.equalToPath;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
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
        Todo[] expectedTodos = {
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

        };
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

        assertThat(todos, hasSize(expectedTodos.length));
        assertThat(todos, contains(expectedTodos));
    }
}
