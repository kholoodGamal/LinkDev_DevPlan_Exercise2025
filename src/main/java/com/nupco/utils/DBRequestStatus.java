package com.nupco.utils;

import com.shaft.tools.io.ReportManager;

public class DBRequestStatus {
    private DBRequestStatus() {
    }

    public static void waitForRequestStatus(
            DatabaseRepository repository,
            String connectionString,
            String sqlQuery,
            String expectedStatus) throws InterruptedException {

        ReportManager.log("Waiting for request status: " + expectedStatus);
        repository.waitForStatusChange(connectionString, sqlQuery, expectedStatus);
    }
}
