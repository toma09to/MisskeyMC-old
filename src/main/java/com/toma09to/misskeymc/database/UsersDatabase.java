package com.toma09to.misskeymc.database;

import com.destroystokyo.paper.profile.PlayerProfile;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class UsersDatabase {
    private final Connection connection;

    public UsersDatabase(String host, String database, String user, String pass) throws SQLException {
        String url = "jdbc:mySQL://" + host + "/" + database;
        connection = DriverManager.getConnection(url, user, pass);
        connection.setAutoCommit(true);
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS authorized_players (
                    uuid TEXT NOT NULL,
                    username TEXT NOT NULL,
                    misskey_user_id TEXT NOT NULL
                )
            """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS tokens (
                    token TEXT NOT NULL,
                    uuid TEXT NOT NULL,
                    username TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
            """);
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public String generateToken(PlayerProfile p) throws SQLException {
        String token = UUID.randomUUID().toString();
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tokens (token, uuid, username, created_at) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setString(1, token);
            preparedStatement.setString(2, p.getId().toString());
            preparedStatement.setString(3, p.getName());
            preparedStatement.setString(4, now);
            preparedStatement.executeUpdate();
        }

        return token;
    }

    public boolean authorizeUser(String token, String userId) throws SQLException {
        ResultSet resultSet;
        String uuid, username;
        LocalDateTime expireDate;

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM tokens WHERE token = ?")) {
            preparedStatement.setString(1, token);
            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) return false;
            uuid = resultSet.getString("uuid");
            username = resultSet.getString("username");
            expireDate = LocalDateTime.parse(resultSet.getString("created_at"), DateTimeFormatter.ISO_DATE_TIME).plusHours(1);
            if (LocalDateTime.now().isAfter(expireDate)) return false;
        }

        // Prevent from register user redundantly
        if (isAuthorized(uuid)) return true;

        // Register authorized user
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO authorized_players (uuid, username, misskey_user_id) VALUES (?, ?, ?)")) {
            preparedStatement.setString(1, uuid);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, userId);
            preparedStatement.executeUpdate();
        }

        // Delete used token
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM tokens WHERE token = ?")) {
            preparedStatement.setString(1, uuid);
            preparedStatement.executeUpdate();
        }
        return true;
    }

    public boolean isAuthorized(String uuid) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM authorized_players WHERE uuid = ?")) {
            preparedStatement.setString(1, uuid);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }
}
