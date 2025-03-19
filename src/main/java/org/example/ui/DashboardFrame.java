package org.example.ui;

import org.example.service.AuthService;
import org.example.util.Config;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private ProductPanel productPanel;
    private SalesPanel salesPanel;
    private ReportPanel reportPanel;

    public DashboardFrame() {
        setTitle(Config.APP_NAME + " v" + Config.APP_VERSION + " - Logged in as: " + AuthService.getCurrentUser().getUsername());
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem exitItem = new JMenuItem("Exit");

        logoutItem.addActionListener(e -> logout());
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        // Initialize panels
        productPanel = new ProductPanel();
        salesPanel = new SalesPanel();
        reportPanel = new ReportPanel();

        // Add tabs
        tabbedPane.addTab("Products", new JScrollPane(productPanel));
        tabbedPane.addTab("Sales", new JScrollPane(salesPanel));
        tabbedPane.addTab("Reports", new JScrollPane(reportPanel));

        // Set access control based on user role
        if (AuthService.getCurrentUser().getRole().equals("CASHIER")) {
            tabbedPane.setEnabledAt(0, false); // Disable product management for cashiers
        }

        // Add tabbed pane to frame
        add(tabbedPane, BorderLayout.CENTER);

        // Add status panel at bottom
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel statusLabel = new JLabel("Ready");
        panel.add(statusLabel, BorderLayout.WEST);

        JLabel userLabel = new JLabel("User: " + AuthService.getCurrentUser().getUsername() +
                " | Role: " + AuthService.getCurrentUser().getRole());
        panel.add(userLabel, BorderLayout.EAST);

        return panel;
    }

    private void logout() {
        AuthService authService = new AuthService();
        authService.logout();
        dispose(); // Close dashboard
        new LoginFrame(); // Open login screen
    }
}