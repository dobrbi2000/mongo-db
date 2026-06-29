package com.example.taskmanager.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public final class MongoConnectionFactory {

    private static final String CONNECTION_URL = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "task_manager_db";

    private static final MongoClient CLIENT = MongoClients.create(CONNECTION_URL);

    private MongoConnectionFactory() {

    }

    public static MongoDatabase getDatabase() {
        return CLIENT.getDatabase(DATABASE_NAME);
    }

    public static void close() {
        CLIENT.close();
    }

}
