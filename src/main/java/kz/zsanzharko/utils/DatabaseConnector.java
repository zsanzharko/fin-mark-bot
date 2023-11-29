package kz.zsanzharko.utils;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class DatabaseConnector {
    private static DatabaseConnector connector;
    private final String url;
    private final String username;
    private final String password;
    private Connection connection;

    private DatabaseConnector(String url, String username,
                              String password, String driverName)
            throws ClassNotFoundException {
        Class.forName(driverName);
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Connection getConnection() {
        try {
            if (connection == null) {
                log.info("Initialization connect to database.");
                return connection = DriverManager.getConnection(url, username, password);
            } else if (connection.isClosed()) {
                log.info("Connection is closed, reconnection...");
                return connection = DriverManager.getConnection(url, username, password);
            }
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static DatabaseConnector getInstance(String url, String username,
                                                String password, String driverName)
            throws ClassNotFoundException {
        if (connector != null) {
            return connector;
        }
        return connector = new DatabaseConnector(url, username, password, driverName);
    }

    public static DatabaseConnector getInstance() {
        if (connector != null) {
            return connector;
        }
        throw new NullPointerException("Database connector is no initialized");
    }
}
