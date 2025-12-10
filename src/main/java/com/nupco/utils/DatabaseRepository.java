package com.nupco.utils;

import com.shaft.tools.io.ReportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DatabaseRepository {
    private final SQLConnectionManager connectionManager;

    @Autowired
    public DatabaseRepository(SQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public List<String> executeQuery(String connectionString, String sql) {
        List<String> rows = new ArrayList<>();
        try (Connection connection = connectionManager.getConnection(connectionString);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String row = createRowString(resultSet);
                List<String> rowElements = List.of(row.split(","));
                rows.addAll(rowElements);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return rows;
    }

    public List<String> executeQueryList(String connectionString, String sql) {
        List<String> rows = new ArrayList<>();
        try (Connection connection = connectionManager.getConnection(connectionString);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                StringBuilder rowBuilder = new StringBuilder();
                int columnCount = resultSet.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String value = resultSet.getString(i);
                    rowBuilder.append(value);
                    if (i < columnCount) {
                        rowBuilder.append(", ");
                    }
                }
                rows.add(rowBuilder.toString());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return rows;
    }

    public void executeUpdate(String connectionString, String sql) {
        try (Connection connection = connectionManager.getConnection(connectionString);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int rowsAffected = statement.executeUpdate();
            System.out.println(rowsAffected);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static String createRowString(ResultSet resultSet) throws SQLException {
        StringBuilder rowBuilder = new StringBuilder();
        int columnCount = resultSet.getMetaData().getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String value = resultSet.getString(i);
            rowBuilder.append(value.trim());
            if (i < columnCount) {
                rowBuilder.append(", ");
            }
        }
        return rowBuilder.toString();
    }

    public void waitForStatusChange(String connectionString, String sqlStatement , String desiredStatus ) throws InterruptedException {
        ReportManager.log("Wait for status to be " + desiredStatus);
        Instant start = Instant.now();
        Duration timeout = Duration.ofMinutes(10);

        while (Duration.between(start, Instant.now()).compareTo(timeout) < 0) {
            List<String> record = executeQuery(connectionString, sqlStatement);
            for (String status : record) {
                if (status.contains(desiredStatus)) {
                    return;
                }
            }
            Thread.sleep(5000);
        }
    }
}
