package com.toma09to.misskeymc.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class UsersDatabase {
    private final Connection connection;

    public UsersDatabase(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE authorized_players (
                    uuid TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    misskey_userid TEXT NOT NULL
                )
            """);
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
