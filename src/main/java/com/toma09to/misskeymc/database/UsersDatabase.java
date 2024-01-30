package com.toma09to.misskeymc.database;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.toma09to.misskeymc.api.MisskeyLogger;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class UsersDatabase {
    private final MisskeyLogger log;
    private final String url;
    private final String username;
    private final String password;

    private Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            log.warning("Failed to connect to the database!");
            log.warning(e.getMessage());
            throw e;
        }
    }

    public UsersDatabase(MisskeyLogger log, String host, String database, String user, String pass) throws SQLException {
        this.log = log;
        url = "jdbc:mySQL://" + host + "/" + database;
        username = user;
        password = pass;

        Connection connection = getConnection();

        try {
            Statement statement = connection.createStatement();
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
        } catch (SQLException e) {
            log.warning("Failed to create tables!");
            log.warning(e.getMessage());
            throw e;
        } finally {
            connection.close();
        }
    }

    public String generateToken(PlayerProfile p) throws SQLException {
        Connection connection = getConnection();

        String token = UUID.randomUUID().toString();
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tokens (token, uuid, username, created_at) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setString(1, token);
            preparedStatement.setString(2, p.getId().toString());
            preparedStatement.setString(3, p.getName());
            preparedStatement.setString(4, now);
            preparedStatement.executeUpdate();
        } finally {
            connection.close();
        }

        return token;
    }

    public boolean authorizeUser(String token, String userId) throws SQLException {
        Connection connection = getConnection();

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
        Connection connection = getConnection();
        boolean ret;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM authorized_players WHERE uuid = ?")) {
            preparedStatement.setString(1, uuid);
            ResultSet resultSet = preparedStatement.executeQuery();
            ret = resultSet.next();
        } finally {
            connection.close();
        }

        return ret;
    }
}
