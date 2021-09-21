package com.exploring.restassured;

public class Todo {
    public Integer id;
    public Integer userId;
    public String title;
    public boolean completed;

    // for jackson.databind
    public Todo() {
    }

    public Todo(Integer userId, Integer id, String title, boolean completed) {
        this.userId = userId;
        this.id = id;
        this.title = title;
        this.completed = completed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Todo todo = (Todo) o;

        if (completed != todo.completed) return false;
        if (!id.equals(todo.id)) return false;
        if (!userId.equals(todo.userId)) return false;
        return title.equals(todo.title);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + userId.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + (completed ? 1 : 0);
        return result;
    }
}
