package com.example.taskmanager.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.model.Subtask;
import com.example.taskmanager.model.Task;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.result.DeleteResult;
import org.bson.types.ObjectId;

public class TaskDao {

    private final MongoCollection<Document> taskCollection;

    public TaskDao(MongoDatabase db) {
        this.taskCollection = db.getCollection("task");
        createIndexs();

    }

    private void createIndexs() {
        taskCollection.createIndex(
                new Document("description", "text")
                        .append("subtasks.name", "text"));
    }

    public String insertTask(Task task) {
        Document doc = TaskMapper.toDocument(task);
        taskCollection.insertOne(doc);
        return doc.getObjectId("_id").toHexString();
    }

    public boolean updateTask(String id, Task updatedTask) {
        try {
            UpdateResult result = taskCollection.updateOne(
                    Filters.eq("_id", new ObjectId(id)),
                    Updates.combine(
                            Updates.set("deadline", TaskMapper.toMongoDate(updatedTask.getDeadline())),
                            Updates.set("name", updatedTask.getName()),
                            Updates.set("description", updatedTask.getDescription()),
                            Updates.set("category", updatedTask.getCategory())));

            return result.getMatchedCount() > 0;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean deleteTask(String id) {
        try {
            DeleteResult result = taskCollection.deleteOne(
                    Filters.eq("_id", new ObjectId(id)));
            return result.getDeletedCount() > 0;
        } catch (IllegalArgumentException e) {
            return false;
        }

    }

    public boolean addSubtask(String taskId, Subtask subtask) {
        try {
            UpdateResult result = taskCollection.updateOne(
                    Filters.eq("_id", new ObjectId(taskId)),
                    Updates.push("subtasks", TaskMapper.toDocument(subtask)));
            return result.getMatchedCount() > 0;
        } catch (IllegalArgumentException e) {
            return false;

        }
    }

    public boolean replaceAllSubtasks(String taskId, List<Subtask> subtasks) {
        try {
            List<Document> subtaskDocuments = new ArrayList<>();

            for (Subtask subtask : subtasks) {
                subtaskDocuments.add(TaskMapper.toDocument(subtask));
            }
            UpdateResult result = taskCollection.updateOne(
                    Filters.eq("_id", new ObjectId(taskId)),
                    Updates.set("subtasks", subtaskDocuments));

            return result.getMatchedCount() > 0;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean deleteAllSubtasks(String taskId) {
        try {
            UpdateResult result = taskCollection.updateOne(
                    Filters.eq("_id", new ObjectId(taskId)),
                    Updates.set("subtasks", new ArrayList<>()));
            return result.getMatchedCount() > 0;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public List<Task> searchByDescriptionWord(String word) {
        if (word == null || word.isBlank()) {
            return new ArrayList<>();
        }

        Pattern pattern = buildWholeWordPattern(word);
        List<Task> result = new ArrayList<>();

        for (Document document : taskCollection.find(Filters.text(word))) {
            String description = document.getString("description");

            if (description != null && pattern.matcher(description).find()) {
                result.add(TaskMapper.fromDocument(document));
            }
        }

        return result;
    }

    public List<Task> searchBySubtaskName(String word) {
        if (word == null || word.isBlank()) {
            return new ArrayList<>();
        }

        Pattern pattern = buildWholeWordPattern(word);
        List<Task> result = new ArrayList<>();

        for (Document document : taskCollection.find(Filters.text(word))) {
            List<Document> subtaskDocs = document.getList("subtasks", Document.class);

            if (subtaskDocs == null) {
                continue;
            }

            boolean matched = false;

            for (Document subtaskDoc : subtaskDocs) {
                String subtaskName = subtaskDoc.getString("name");

                if (subtaskName != null && pattern.matcher(subtaskName).find()) {
                    matched = true;
                    break;
                }
            }

            if (matched) {
                result.add(TaskMapper.fromDocument(document));
            }
        }

        return result;
    }

    public List<Task> findAllTasks() {
        return mapTasks(taskCollection.find());

    }

    public List<Task> findOverdueTasks() {
        return mapTasks(taskCollection.find(Filters.lt("deadline", new Date())));
    }

    public List<Task> findTasksByCategory(String category) {
        return mapTasks(taskCollection.find(Filters.eq("category", category)));
    }

    public List<Document> findSubtasksByCategory(String category) {
        List<Bson> pipeline = List.of(
                new Document("$match", new Document("category", category)),
                new Document("$unwind", "$subtasks"),
                new Document("$project", new Document("_id", 0)
                        .append("taskName", "$name")
                        .append("category", "$category")
                        .append("subtaskName", "$subtasks.name")
                        .append("subtaskDescription", "$subtasks.description")));

        return taskCollection.aggregate(pipeline).into(new ArrayList<>());
    }

    private List<Task> mapTasks(FindIterable<Document> docs) {
        List<Task> tasks = new ArrayList<>();
        for (Document doc : docs) {
            tasks.add(TaskMapper.fromDocument(doc));
        }
        return tasks;
    }

    private Pattern buildWholeWordPattern(String word) {
        return Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
    }

    // TASK 1
    public List<Document> countTasksByCategory() {
        List<Bson> pipeline = List.of(
                new Document("$group",
                        new Document("_id", "$category")
                                .append("taskCount", new Document("$sum", 1))),
                new Document("$sort", new Document("taskCount", -1)));

        return taskCollection.aggregate(pipeline).into(new ArrayList<>());
    }

}
