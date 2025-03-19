package org.example;

import org.example.ui.LoginFrame;
import org.example.util.DatabaseUtil;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize database connection
        if (DatabaseUtil.testConnection()) {
            System.out.println("Database connection successful");
        } else {
            JOptionPane.showMessageDialog(null,
                    "Failed to connect to the database. Please check your configuration.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Start application with login screen
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}