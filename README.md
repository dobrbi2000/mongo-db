# MongoDB Task Manager

Console Java application for managing tasks and subtasks using MongoDB.

## Project Description

This project is a simple **console-based Task Manager** built with:

- **Java**
- **Maven**
- **MongoDB**

The application allows the user to create, update, delete, and search tasks stored in MongoDB.

Each task contains:
- creation date
- deadline
- name
- description
- category
- list of subtasks

Each subtask contains:
- name
- description

---

## Features

The application supports the following operations:

### Task operations
- Insert a new task
- Update an existing task
- Delete a task
- Display all tasks
- Display overdue tasks
- Display tasks by category

### Subtask operations
- Add a subtask to a task
- Replace all subtasks of a task
- Delete all subtasks of a task
- Display all subtasks related to tasks with a specific category

### Search operations
- Full-text search by word in task description
- Full-text search by subtask name

---

## Technologies Used

- **Java 17+**
- **Maven**
- **MongoDB**
- **MongoDB Java Driver (Sync)**

---

## Data Model

### Task
Each task contains the following fields:

- `id`
- `createdAt`
- `deadline`
- `name`
- `description`
- `category`
- `subtasks`

### Subtask
Each subtask contains:

- `name`
- `description`

---

## MongoDB Document Example

```json
{
  "_id": ObjectId("685a48f5c0c4dc3b8e2b1f91"),
  "createdAt": ISODate("2026-06-29T10:00:00Z"),
  "deadline": ISODate("2026-07-02T18:00:00Z"),
  "name": "Prepare MongoDB project",
  "description": "Create DAO layer and console menu",
  "category": "study",
  "subtasks": [
    {
      "name": "Write DAO",
      "description": "Implement CRUD operations"
    },
    {
      "name": "Create menu",
      "description": "Add console interaction"
    }
  ]
}
```
## Project Structure

```text
src/main/java/com/example/taskmanager
├── config
│   └── MongoConnectionFactory.java
├── dao
│   └── TaskDao.java
├── mapper
│   └── TaskMapper.java
├── model
│   ├── Task.java
│   └── Subtask.java
├── TaskManagerApp.java
└── TaskSeederApp.java
```

# Task 1 — Backend Database Migration (SQL → MongoDB)

## Overview

This task demonstrates migration of an existing backend application from a relational SQL database to **MongoDB**.

The migration is implemented on top of the **MongoDB Task Manager** application and includes:
- SQL data source  
- MongoDB target database  
- Data model redesign  
- Data migration job  
- Aggregation query using MongoDB  

---

## Database Technologies

### Source Database (SQL)

- **Database:** H2 (embedded)  
- **Access:** JDBC  
- **Purpose:** Simulated legacy relational database  

### Target Database (NoSQL)

- **Database:** MongoDB  
-- **Driver:** MongoDB Java Driver (Sync)  
- **Data model:** Document-oriented  

---

## SQL Data Model

### `tasks` table

| Column      | Type        |
|-------------|-------------|
| id          | BIGINT (PK) |
| created_at  | TIMESTAMP   |
| deadline    | TIMESTAMP   |
| name        | VARCHAR     |
| description | VARCHAR     |
| category    | VARCHAR     |

### `subtasks` table

| Column      | Type                   |
|-------------|------------------------|
| id          | BIGINT (PK)            |
| task_id     | BIGINT (FK → tasks.id) |
| name        | VARCHAR                |
| description | VARCHAR                |

The SQL schema represents a normalized relational structure with a **one-to-many** relationship between tasks and subtasks.

---

## MongoDB Data Model

Each task is stored as a **single MongoDB document** with **embedded subtasks**.

```json
{
  "_id": ObjectId("..."),
  "createdAt": ISODate("2026-06-29T10:00:00Z"),
  "deadline": ISODate("2026-07-02T18:00:00Z"),
  "name": "Prepare MongoDB project",
  "description": "Create DAO layer and console menu",
  "category": "study",
  "subtasks": [
    {
      "name": "Write DAO",
      "description": "Implement CRUD operations"
    }
  ]
}
```
## Example Output

```text
Aggregation result: tasks grouped by category
{"_id":"work","taskCount":2}
{"_id":"study","taskCount":2}
{"_id":"home","taskCount":1}
```

## Project Structure

```text
src/main/java/com/example/taskmanager
├── config
│   ├── MongoConnectionFactory.java
│   └── SqlConnectionFactory.java
├── dao
│   ├── TaskDao.java
│   └── SqlTaskReaderDao.java
├── migration
│   └── SqlToMongoMigrationApp.java
```