package com.example.taskmanager;

import com.example.taskmanager.config.MongoConnectionFactory;
import com.example.taskmanager.dao.TaskDao;
import com.example.taskmanager.model.Subtask;
import com.example.taskmanager.model.Task;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskSeederApp {

    public static void main(String[] args) {
        TaskDao taskDao = new TaskDao(MongoConnectionFactory.getDatabase());

        try {

            clearTasksCollection();

            seedTasks(taskDao, 100);

            System.out.println("Seed completed: 100 tasks inserted.");
        } finally {
            MongoConnectionFactory.close();
        }
    }

    private static void clearTasksCollection() {
        MongoCollection<Document> collection = MongoConnectionFactory.getDatabase().getCollection("tasks");

        collection.deleteMany(new Document());
    }

    private static void seedTasks(TaskDao taskDao, int count) {
        for (int i = 1; i <= count; i++) {
            Task task = new Task();

            task.setCreatedAt(LocalDateTime.now().minusDays(i % 30));
            task.setDeadline(LocalDateTime.now().plusDays((i % 20) - 10));
            task.setName("Task " + i);
            task.setDescription("Description for task " + i + " with mongodb search dao menu");
            task.setCategory(getCategory(i));
            task.setSubtasks(createSubtasks(i));

            taskDao.insertTask(task);
        }
    }

    private static List<Subtask> createSubtasks(int i) {
        List<Subtask> subtasks = new ArrayList<>();

        subtasks.add(new Subtask(
                "Subtask A" + i,
                "First subtask for task " + i));

        subtasks.add(new Subtask(
                "Subtask B" + i,
                "Second subtask for task " + i));

        return subtasks;
    }

    private static String getCategory(int i) {
        return switch (i % 4) {
            case 0 -> "study";
            case 1 -> "work";
            case 2 -> "home";
            default -> "sport";
        };
    }
}