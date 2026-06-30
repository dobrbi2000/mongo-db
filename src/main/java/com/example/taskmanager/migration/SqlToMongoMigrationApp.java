package com.example.taskmanager.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import com.example.taskmanager.config.MongoConnectionFactory;
import com.example.taskmanager.config.SqlConnectionFactory;
import com.example.taskmanager.dao.SqlTaskReaderDao;
import com.example.taskmanager.dao.TaskDao;
import com.example.taskmanager.model.Task;

public class SqlToMongoMigrationApp {

    public static void main(String[] args) {
        TaskDao mongoTaskDao = new TaskDao(MongoConnectionFactory.getDatabase());
        SqlTaskReaderDao sqlTaskReaderDao = new SqlTaskReaderDao();

        try {
            initSqlSchema();
            seedSqlData();
            clearMongoCollection();
            migrateTasks(sqlTaskReaderDao, mongoTaskDao);
            printAggregationResult(mongoTaskDao);

            System.out.println("Migration completed.");
        } finally {
            MongoConnectionFactory.close();
        }
    }

    private static void initSqlSchema() {
        String createTasksTable = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id BIGINT PRIMARY KEY,
                    created_at TIMESTAMP NOT NULL,
                    deadline TIMESTAMP NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    description VARCHAR(1000) NOT NULL,
                    category VARCHAR(100) NOT NULL
                )
                """;

        String createSubtasksTable = """
                CREATE TABLE IF NOT EXISTS subtasks (
                    id BIGINT PRIMARY KEY,
                    task_id BIGINT NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    description VARCHAR(1000) NOT NULL,
                    CONSTRAINT fk_subtasks_task
                        FOREIGN KEY (task_id) REFERENCES tasks(id)
                )
                """;

        try (Connection connection = SqlConnectionFactory.getConnection();
                PreparedStatement tasksStatement = connection.prepareStatement(createTasksTable);
                PreparedStatement subtasksStatement = connection.prepareStatement(createSubtasksTable)) {

            tasksStatement.execute();
            subtasksStatement.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create SQL schema", e);
        }
    }

    private static void seedSqlData() {
        LocalDateTime now = LocalDateTime.now();

        try (Connection connection = SqlConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement deleteSubtasks = connection.prepareStatement("DELETE FROM subtasks");
                    PreparedStatement deleteTasks = connection.prepareStatement("DELETE FROM tasks");
                    PreparedStatement insertTask = connection.prepareStatement("""
                            INSERT INTO tasks (id, created_at, deadline, name, description, category)
                            VALUES (?, ?, ?, ?, ?, ?)
                            """);
                    PreparedStatement insertSubtask = connection.prepareStatement("""
                            INSERT INTO subtasks (id, task_id, name, description)
                            VALUES (?, ?, ?, ?)
                            """)) {

                deleteSubtasks.executeUpdate();
                deleteTasks.executeUpdate();

                addTask(insertTask, 1, now.minusDays(10), now.minusDays(1),
                        "Prepare report", "Prepare monthly report for management", "work");
                addTask(insertTask, 2, now.minusDays(7), now.plusDays(2),
                        "Study MongoDB", "Read MongoDB docs and build examples", "study");
                addTask(insertTask, 3, now.minusDays(5), now.plusDays(5),
                        "Buy groceries", "Buy products for the week", "home");
                addTask(insertTask, 4, now.minusDays(3), now.minusHours(5),
                        "Fix bug", "Fix task manager search bug", "work");
                addTask(insertTask, 5, now.minusDays(1), now.plusDays(7),
                        "Prepare presentation", "Create slides about NoSQL databases", "study");

                addSubtask(insertSubtask, 1, 1, "Collect data", "Gather all report metrics");
                addSubtask(insertSubtask, 2, 1, "Write summary", "Prepare short business summary");

                addSubtask(insertSubtask, 3, 2, "Read docs", "Read official MongoDB documentation");
                addSubtask(insertSubtask, 4, 2, "Create demo", "Build a small practice app");

                addSubtask(insertSubtask, 5, 3, "Make list", "Prepare shopping list");
                addSubtask(insertSubtask, 6, 3, "Visit store", "Buy all required products");

                addSubtask(insertSubtask, 7, 4, "Reproduce bug", "Find exact failing case");
                addSubtask(insertSubtask, 8, 4, "Write fix", "Fix search implementation");

                addSubtask(insertSubtask, 9, 5, "Create plan", "Prepare presentation structure");
                addSubtask(insertSubtask, 10, 5, "Design slides", "Create slide deck");

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to seed SQL data", e);
        }
    }

    private static void clearMongoCollection() {
        MongoCollection<Document> collection = MongoConnectionFactory.getDatabase().getCollection("tasks");

        collection.deleteMany(new Document());
    }

    private static void migrateTasks(SqlTaskReaderDao sqlTaskReaderDao, TaskDao mongoTaskDao) {
        List<Task> tasks = sqlTaskReaderDao.findAllTasksWithSubtasks();

        for (Task task : tasks) {
            task.setId(null); // MongoDB сам создаст ObjectId
            mongoTaskDao.insertTask(task);
        }

        System.out.println("Migrated tasks: " + tasks.size());
    }

    private static void printAggregationResult(TaskDao mongoTaskDao) {
        System.out.println();
        System.out.println("Aggregation result: tasks grouped by category");

        for (Document document : mongoTaskDao.countTasksByCategory()) {
            System.out.println(document.toJson());
        }
    }

    private static void addTask(
            PreparedStatement statement,
            long id,
            LocalDateTime createdAt,
            LocalDateTime deadline,
            String name,
            String description,
            String category) throws SQLException {
        statement.setLong(1, id);
        statement.setTimestamp(2, Timestamp.valueOf(createdAt));
        statement.setTimestamp(3, Timestamp.valueOf(deadline));
        statement.setString(4, name);
        statement.setString(5, description);
        statement.setString(6, category);
        statement.executeUpdate();
    }

    private static void addSubtask(
            PreparedStatement statement,
            long id,
            long taskId,
            String name,
            String description) throws SQLException {
        statement.setLong(1, id);
        statement.setLong(2, taskId);
        statement.setString(3, name);
        statement.setString(4, description);
        statement.executeUpdate();
    }
}