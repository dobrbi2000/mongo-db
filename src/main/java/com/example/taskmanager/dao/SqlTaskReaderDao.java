package com.example.taskmanager.dao;

import com.example.taskmanager.config.SqlConnectionFactory;
import com.example.taskmanager.model.Subtask;
import com.example.taskmanager.model.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SqlTaskReaderDao {

    private static final String FIND_ALL_TASKS_WITH_SUBTASKS = """
            SELECT
                t.id AS task_id,
                t.created_at,
                t.deadline,
                t.name AS task_name,
                t.description AS task_description,
                t.category,
                s.id AS subtask_id,
                s.name AS subtask_name,
                s.description AS subtask_description
            FROM tasks t
            LEFT JOIN subtasks s ON s.task_id = t.id
            ORDER BY t.id, s.id
            """;

    public List<Task> findAllTasksWithSubtasks() {
        Map<Long, Task> tasks = new LinkedHashMap<>();

        try (Connection connection = SqlConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_ALL_TASKS_WITH_SUBTASKS);
                ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                long taskId = rs.getLong("task_id");

                Task task = tasks.get(taskId);
                if (task == null) {
                    task = new Task();
                    task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    task.setDeadline(rs.getTimestamp("deadline").toLocalDateTime());
                    task.setName(rs.getString("task_name"));
                    task.setDescription(rs.getString("task_description"));
                    task.setCategory(rs.getString("category"));
                    task.setSubtasks(new ArrayList<>());

                    tasks.put(taskId, task);
                }

                long subtaskId = rs.getLong("subtask_id");
                if (!rs.wasNull()) {
                    task.getSubtasks().add(new Subtask(
                            rs.getString("subtask_name"),
                            rs.getString("subtask_description")));
                }
            }

            return new ArrayList<>(tasks.values());

        } catch (SQLException e) {
            throw new RuntimeException("Failed to read tasks from SQL database", e);
        }
    }
}