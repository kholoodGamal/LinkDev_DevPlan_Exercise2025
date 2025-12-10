package com.nupco.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SQLConnectionManager {

    private static final String DEFAULT_DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    public Connection getConnection(String connectionString) throws SQLException {
        // Load the driver
        String driverClass = getProperty("jdbc.driver", DEFAULT_DRIVER_CLASS);
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            logException("Failed to load JDBC driver: " + e.getMessage());
            throw new SQLException("Driver not found: " + driverClass);
        }
        // Set the connection timeout

        Properties properties = new Properties();
        properties.setProperty("loginTimeout", "10"); // Set the timeout value in seconds
        properties.setProperty("connectTimeout", "5000"); // Set the timeout value in millisecond

        // Get the connection URL

        String url = getProperty("jdbc.url", connectionString);

        try {
            Connection connection = DriverManager.getConnection(url, properties);
            if (!connection.isClosed()) {

            } else {
                System.out.println("Connection is closed.");
            }
            return connection;
        } catch (SQLException e) {
            logException("Error connecting to the database: " + e.getMessage());
            throw e; // Rethrow the exception for caller handling
        }
    }

    private String getProperty(String key, String defaultValue) {
        // Implement logic to retrieve property from configuration, environment, etc.
        // You can use libraries like Apache Commons Configuration for this.
        return System.getProperty(key, defaultValue);
    }

    private void logException(String message) {
        // Implement logging using a suitable logging framework
        // (e.g., Log4j, SLF4J)
        System.err.println(message);
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Handle the exception or log it
            }
        }
    }
}
