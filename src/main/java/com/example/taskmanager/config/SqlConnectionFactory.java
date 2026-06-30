package com.example.taskmanager.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class SqlConnectionFactory {

    private static final String URL = "jdbc:h2:./legacy-task-manager-db";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private SqlConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}