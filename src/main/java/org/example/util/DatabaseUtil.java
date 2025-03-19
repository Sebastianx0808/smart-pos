package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(
                        Config.DB_URL,
                        Config.DB_USER,
                        Config.DB_PASSWORD
                );
            } catch (SQLException e) {
                System.err.println("Database connection error: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Tests the database connection.
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try {
            Connection testConn = getConnection();
            boolean isValid = testConn != null && !testConn.isClosed() && testConn.isValid(5);
            return isValid;
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
}