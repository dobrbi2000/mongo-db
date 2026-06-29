package com.example.taskmanager;

import com.example.taskmanager.config.MongoConnectionFactory;
import com.example.taskmanager.dao.TaskDao;
import com.example.taskmanager.model.Subtask;
import com.example.taskmanager.model.Task;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TaskManagerApp {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        TaskDao taskDao = new TaskDao(MongoConnectionFactory.getDatabase());

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;

            while (running) {
                printMenu();
                String choice = scanner.nextLine();

                try {
                    switch (choice) {
                        case "1" -> printTasks(taskDao.findAllTasks());

                        case "2" -> printTasks(taskDao.findOverdueTasks());

                        case "3" -> {
                            String category = readRequired(scanner, "Enter category: ");
                            printTasks(taskDao.findTasksByCategory(category));
                        }

                        case "4" -> {
                            String category = readRequired(scanner, "Enter category: ");
                            printSubtasksByCategory(taskDao.findSubtasksByCategory(category));
                        }

                        case "5" -> createTask(scanner, taskDao);

                        case "6" -> updateTask(scanner, taskDao);

                        case "7" -> deleteTask(scanner, taskDao);

                        case "8" -> addSubtask(scanner, taskDao);

                        case "9" -> replaceAllSubtasks(scanner, taskDao);

                        case "10" -> deleteAllSubtasks(scanner, taskDao);

                        case "11" -> {
                            String word = readRequired(scanner, "Enter word for description search: ");
                            printTasks(taskDao.searchByDescriptionWord(word));
                        }

                        case "12" -> {
                            String word = readRequired(scanner, "Enter word for subtask name search: ");
                            printTasks(taskDao.searchBySubtaskName(word));
                        }

                        case "0" -> {
                            running = false;
                            System.out.println("Application stopped.");
                        }

                        default -> System.out.println("Unknown menu item.");
                    }
                } catch (Exception e) {
                    System.out.println("Operation failed: " + e.getMessage());
                }
            }
        } finally {
            MongoConnectionFactory.close();
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("===== TASK MANAGER =====");
        System.out.println("1. Show all tasks");
        System.out.println("2. Show overdue tasks");
        System.out.println("3. Show tasks by category");
        System.out.println("4. Show subtasks by category");
        System.out.println("5. Create task");
        System.out.println("6. Update task");
        System.out.println("7. Delete task");
        System.out.println("8. Add subtask to task");
        System.out.println("9. Replace all subtasks of task");
        System.out.println("10. Delete all subtasks of task");
        System.out.println("11. Search by word in task description");
        System.out.println("12. Search by word in subtask name");
        System.out.println("0. Exit");
        System.out.print("Choose option: ");
    }

    private static void createTask(Scanner scanner, TaskDao taskDao) {
        Task task = new Task();
        task.setCreatedAt(LocalDateTime.now());
        task.setDeadline(readDateTime(scanner, "Enter deadline (yyyy-MM-dd HH:mm): "));
        task.setName(readRequired(scanner, "Enter task name: "));
        task.setDescription(readRequired(scanner, "Enter task description: "));
        task.setCategory(readRequired(scanner, "Enter category: "));
        task.setSubtasks(readSubtasks(scanner));

        String id = taskDao.insertTask(task);
        System.out.println("Task created with id: " + id);
    }

    private static void updateTask(Scanner scanner, TaskDao taskDao) {
        String id = readRequired(scanner, "Enter task id: ");

        Task updatedTask = new Task();
        updatedTask.setDeadline(readDateTime(scanner, "Enter new deadline (yyyy-MM-dd HH:mm): "));
        updatedTask.setName(readRequired(scanner, "Enter new task name: "));
        updatedTask.setDescription(readRequired(scanner, "Enter new task description: "));
        updatedTask.setCategory(readRequired(scanner, "Enter new category: "));

        boolean updated = taskDao.updateTask(id, updatedTask);
        System.out.println(updated ? "Task updated." : "Task not found or invalid id.");
    }

    private static void deleteTask(Scanner scanner, TaskDao taskDao) {
        String id = readRequired(scanner, "Enter task id: ");
        boolean deleted = taskDao.deleteTask(id);
        System.out.println(deleted ? "Task deleted." : "Task not found or invalid id.");
    }

    private static void addSubtask(Scanner scanner, TaskDao taskDao) {
        String taskId = readRequired(scanner, "Enter task id: ");
        String subtaskName = readRequired(scanner, "Enter subtask name: ");
        String subtaskDescription = readRequired(scanner, "Enter subtask description: ");

        boolean added = taskDao.addSubtask(taskId, new Subtask(subtaskName, subtaskDescription));
        System.out.println(added ? "Subtask added." : "Task not found or invalid id.");
    }

    private static void replaceAllSubtasks(Scanner scanner, TaskDao taskDao) {
        String taskId = readRequired(scanner, "Enter task id: ");
        List<Subtask> subtasks = readSubtasks(scanner);

        boolean updated = taskDao.replaceAllSubtasks(taskId, subtasks);
        System.out.println(updated ? "All subtasks replaced." : "Task not found or invalid id.");
    }

    private static void deleteAllSubtasks(Scanner scanner, TaskDao taskDao) {
        String taskId = readRequired(scanner, "Enter task id: ");
        boolean deleted = taskDao.deleteAllSubtasks(taskId);
        System.out.println(deleted ? "All subtasks deleted." : "Task not found or invalid id.");
    }

    private static List<Subtask> readSubtasks(Scanner scanner) {
        int count = readInt(scanner, "How many subtasks? ");
        List<Subtask> subtasks = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            System.out.println("Subtask #" + i);
            String name = readRequired(scanner, "  Enter subtask name: ");
            String description = readRequired(scanner, "  Enter subtask description: ");
            subtasks.add(new Subtask(name, description));
        }

        return subtasks;
    }

    private static LocalDateTime readDateTime(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();

            try {
                return LocalDateTime.parse(input, FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Wrong format. Use yyyy-MM-dd HH:mm");
            }
        }
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    private static String readRequired(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();

            if (value != null && !value.isBlank()) {
                return value.trim();
            }

            System.out.println("Value must not be blank.");
        }
    }

    private static void printTasks(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }

        for (Task task : tasks) {
            System.out.println("-----------------------------------");
            System.out.println(task);
        }
    }

    private static void printSubtasksByCategory(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            System.out.println("No subtasks found for this category.");
            return;
        }

        for (Document document : documents) {
            System.out.println("-----------------------------------");
            System.out.println("Task name: " + document.getString("taskName"));
            System.out.println("Category: " + document.getString("category"));
            System.out.println("Subtask name: " + document.getString("subtaskName"));
            System.out.println("Subtask description: " + document.getString("subtaskDescription"));
        }
    }
}