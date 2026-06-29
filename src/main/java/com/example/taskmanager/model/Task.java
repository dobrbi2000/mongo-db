package com.example.taskmanager.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task {

    private String id;
    private LocalDateTime createdAt;
    private LocalDateTime deadline;
    private String name;
    private String description;
    private String category;
    private List<Subtask> subtasks = new ArrayList<>();

    public Task() {

    }

    public Task(
            String id,
            LocalDateTime createdAt,
            LocalDateTime deadline,
            String name,
            String description,
            String category,
            List<Subtask> subtasks) {
        this.id = id;
        this.createdAt = createdAt;
        this.deadline = deadline;
        this.name = name;
        this.description = description;
        this.category = category;
        this.subtasks = subtasks;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", createdAt=" + createdAt +
                ", deadline=" + deadline +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", subtasks=" + subtasks +
                '}';
    }

}
