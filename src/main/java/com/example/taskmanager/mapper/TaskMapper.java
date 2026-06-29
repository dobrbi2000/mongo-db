package com.example.taskmanager.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.example.taskmanager.model.Subtask;
import com.example.taskmanager.model.Task;

public final class TaskMapper {

    private TaskMapper() {
    }

    public static Document toDocument(Task task) {
        Document doc = new Document();

        if (task.getId() != null && !task.getId().isBlank()) {
            doc.put("_id", new ObjectId(task.getId()));
        }

        doc.put("createdAt", toMongoDate(task.getCreatedAt()));
        doc.put("deadline", toMongoDate(task.getDeadline()));
        doc.put("name", task.getName());
        doc.put("description", task.getDescription());
        doc.put("category", task.getCategory());

        List<Document> subTaskDocs = new ArrayList<>();

        if (task.getSubtasks() != null) {
            for (Subtask subtask : task.getSubtasks()) {
                subTaskDocs.add(toDocumentSubTask(subtask));
            }
        }

        doc.put("subtasks", subTaskDocs);
        return doc;

    }

    public static Document toDocument(Subtask subtask) {
        return new Document("name", subtask.getName())
                .append("description", subtask.getDescription());
    }

    public static Task fromDocument(Document doc) {
        Task task = new Task();

        ObjectId id = doc.getObjectId("_id");
        if (id != null) {
            task.setId(id.toHexString());
        }

        task.setCreatedAt(fromMongoDate(doc.getDate("createdAt")));
        task.setDeadline(fromMongoDate(doc.getDate("deadline")));
        task.setName(doc.getString("name"));
        task.setDescription(doc.getString("description"));
        task.setCategory(doc.getString("category"));

        List<Subtask> subtasks = new ArrayList<>();
        List<Document> subtasksDocs = doc.getList("subtasks", Document.class);

        if (subtasksDocs != null) {
            for (Document subtaskDoc : subtasksDocs) {
                subtasks.add(new Subtask(
                        subtaskDoc.getString("name"),
                        subtaskDoc.getString("description")));
            }

        }
        task.setSubtasks(subtasks);
        return task;

    }

    public static Date toMongoDate(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Document toDocumentSubTask(Subtask subtask) {
        return new Document("name", subtask.getName())
                .append("description", subtask.getDescription());
    }

    public static LocalDateTime fromMongoDate(Date value) {
        if (value == null) {
            return null;
        }
        return LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
    }

}
