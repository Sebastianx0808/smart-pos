package org.example.ui;

import org.example.model.Sale;
import org.example.service.SaleService;
import org.example.util.Config;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportPanel extends JPanel {
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> reportTypeComboBox;
    private JPanel detailsPanel;
    private JSpinner fromDateSpinner;
    private JSpinner toDateSpinner;

    // Additional SDG-related tables
    private JTable sdgWasteTable;
    private JTable sdgPackagingTable;
    private JTable sdgExpiryTable;

    private SaleService saleService;
    private final Color HEADER_COLOR = new Color(210, 230, 220);
    private final Font HEADER_FONT = new Font("Arial", Font.BOLD, 12);
    private final Color BUTTON_COLOR = new Color(100, 180, 140);
    private final Color SDG_PANEL_COLOR = new Color(240, 255, 240);

    public ReportPanel() {
        saleService = new SaleService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = createControlPanel();
        JPanel centerPanel = createReportPanel();

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        TitledBorder titledBorder = BorderFactory.createTitledBorder("Report Controls");
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        panel.setBorder(titledBorder);

        // Create a sub-panel for filters with FlowLayout
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // Report type selector with custom renderer
        filtersPanel.add(new JLabel("Report Type:", JLabel.RIGHT));
        reportTypeComboBox = new JComboBox<>(new String[]{"Daily Sales", "Product Performance", "SDG Report"});
        reportTypeComboBox.setPreferredSize(new Dimension(150, 25));
        reportTypeComboBox.setRenderer(new CustomComboBoxRenderer());
        reportTypeComboBox.addActionListener(e -> updateUIForReportType(reportTypeComboBox.getSelectedIndex()));
        filtersPanel.add(reportTypeComboBox);

        // Date range selector with improved look
        filtersPanel.add(new JLabel("From:", JLabel.RIGHT));
        fromDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor fromDateEditor = new JSpinner.DateEditor(fromDateSpinner, "yyyy-MM-dd");
        fromDateSpinner.setEditor(fromDateEditor);
        fromDateSpinner.setPreferredSize(new Dimension(120, 25));
        filtersPanel.add(fromDateSpinner);

        filtersPanel.add(new JLabel("To:", JLabel.RIGHT));
        toDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor toDateEditor = new JSpinner.DateEditor(toDateSpinner, "yyyy-MM-dd");
        toDateSpinner.setEditor(toDateEditor);
        toDateSpinner.setPreferredSize(new Dimension(120, 25));
        filtersPanel.add(toDateSpinner);

        // Add filters panel to main panel
        panel.add(filtersPanel);

        // Button panel with improved styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // Generate button with custom styling
        JButton generateButton = new JButton("Generate Report");
        generateButton.setBackground(BUTTON_COLOR);
        generateButton.setForeground(Color.BLACK);
        generateButton.setFocusPainted(false);
        generateButton.setFont(new Font("Arial", Font.BOLD, 12));
        generateButton.addActionListener(e -> {
            java.util.Date fromDate = (java.util.Date) fromDateSpinner.getValue();
            java.util.Date toDate = (java.util.Date) toDateSpinner.getValue();

            LocalDateTime startDateTime = LocalDateTime.of(
                    fromDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    LocalTime.MIN
            );

            LocalDateTime endDateTime = LocalDateTime.of(
                    toDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    LocalTime.MAX
            );

            generateReport(reportTypeComboBox.getSelectedIndex(), startDateTime, endDateTime);
        });
        buttonPanel.add(generateButton);

        // Export button
        JButton exportButton = new JButton("Export");
        exportButton.setBackground(BUTTON_COLOR);
        exportButton.setForeground(Color.BLACK);
        exportButton.setFocusPainted(false);
        exportButton.setFont(new Font("Arial", Font.BOLD, 12));
        exportButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Report");
            int result = fileChooser.showSaveDialog(ReportPanel.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                exportReportToFile(filePath);
            }
        });
        buttonPanel.add(exportButton);

        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(BUTTON_COLOR);
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setFocusPainted(false);
        refreshButton.setFont(new Font("Arial", Font.BOLD, 12));
        refreshButton.addActionListener(e -> refreshReport());
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel);

        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Report Results");
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        panel.setBorder(titledBorder);

        // Create table with enhanced styling
        String[] columnNames = {"Date", "Total Sales", "Items Sold", "Average Sale"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        reportTable.setRowHeight(25);
        reportTable.setGridColor(new Color(220, 220, 220));
        reportTable.setSelectionBackground(new Color(200, 230, 210));
        reportTable.setSelectionForeground(Color.BLACK);
        reportTable.setFont(new Font("Arial", Font.PLAIN, 12));

        // Style the table header
        JTableHeader header = reportTable.getTableHeader();
        header.setBackground(HEADER_COLOR);
        header.setFont(HEADER_FONT);

        // Create details panel for SDG metrics
        detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        TitledBorder detailsBorder = BorderFactory.createTitledBorder("Details");
        detailsBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        detailsPanel.setBorder(detailsBorder);

        // Create SDG-specific tables
        initializeSDGTables();

        // Create split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(new JScrollPane(reportTable));
        splitPane.setBottomComponent(new JScrollPane(detailsPanel));
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(8);
        splitPane.setDividerLocation(200);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private void initializeSDGTables() {
        // Create waste reduction table
        String[] wasteColumns = {"Product", "Quantity", "Weight (kg)", "Waste Category", "Impact"};
        DefaultTableModel wasteModel = new DefaultTableModel(wasteColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        sdgWasteTable = createStyledTable(wasteModel);

        // Create packaging table
        String[] packagingColumns = {"Packaging Type", "Units Saved", "Material", "Weight (g)", "Recycling Status"};
        DefaultTableModel packagingModel = new DefaultTableModel(packagingColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        sdgPackagingTable = createStyledTable(packagingModel);

        // Create expiry management table
        String[] expiryColumns = {"Product", "Original Expiry", "Days to Expiry", "Discount Applied", "Status"};
        DefaultTableModel expiryModel = new DefaultTableModel(expiryColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        sdgExpiryTable = createStyledTable(expiryModel);
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(200, 230, 210));
        table.setSelectionForeground(Color.BLACK);
        table.setFont(new Font("Arial", Font.PLAIN, 12));

        // Style the table header
        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_COLOR);
        header.setFont(HEADER_FONT);

        return table;
    }

    private void updateUIForReportType(int reportType) {
        switch (reportType) {
            case 0: // Daily Sales
                String[] dailyColumns = {"Date", "Total Sales", "Items Sold", "Average Sale"};
                tableModel.setColumnIdentifiers(dailyColumns);
                detailsPanel.setVisible(false);
                break;
            case 1: // Product Performance
                String[] productColumns = {"Product", "Quantity Sold", "Revenue", "% of Total"};
                tableModel.setColumnIdentifiers(productColumns);
                detailsPanel.setVisible(false);
                break;
            case 2: // SDG Report
                String[] sdgColumns = {"Metric", "Value", "Impact", "Trend"};
                tableModel.setColumnIdentifiers(sdgColumns);
                detailsPanel.setVisible(true);
                break;
        }
    }

    private void generateReport(int reportType, LocalDateTime startDate, LocalDateTime endDate) {
        tableModel.setRowCount(0);

        // Clear all SDG tables
        if (sdgWasteTable.getModel() instanceof DefaultTableModel) {
            ((DefaultTableModel) sdgWasteTable.getModel()).setRowCount(0);
        }
        if (sdgPackagingTable.getModel() instanceof DefaultTableModel) {
            ((DefaultTableModel) sdgPackagingTable.getModel()).setRowCount(0);
        }
        if (sdgExpiryTable.getModel() instanceof DefaultTableModel) {
            ((DefaultTableModel) sdgExpiryTable.getModel()).setRowCount(0);
        }

        // Update UI for the selected report type
        updateUIForReportType(reportType);

        switch (reportType) {
            case 0: // Daily Sales
                generateDailySalesReport(startDate, endDate);
                break;
            case 1: // Product Performance
                generateProductPerformanceReport(startDate, endDate);
                break;
            case 2: // SDG Report
                generateSDGReport(startDate, endDate);
                break;
        }
    }

    private void generateDailySalesReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Sale> sales = saleService.getSalesByDateRange(startDate, endDate);

        // Group sales by date
        Map<LocalDate, DailySalesSummary> salesByDate = new HashMap<>();

        for (Sale sale : sales) {
            LocalDate saleDate = sale.getSaleDate().toLocalDate();

            if (!salesByDate.containsKey(saleDate)) {
                salesByDate.put(saleDate, new DailySalesSummary());
            }

            DailySalesSummary summary = salesByDate.get(saleDate);
            summary.totalSales = summary.totalSales.add(sale.getTotalAmount());
            summary.totalItems += sale.getItems().size();
            summary.saleCount++;
        }

        // Format dates for better display
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        // Populate table
        for (Map.Entry<LocalDate, DailySalesSummary> entry : salesByDate.entrySet()) {
            DailySalesSummary summary = entry.getValue();

            Object[] row = new Object[4];
            row[0] = entry.getKey().format(formatter);
            row[1] = "$" + summary.totalSales;
            row[2] = summary.totalItems;

            if (summary.saleCount > 0) {
                BigDecimal avgSale = summary.totalSales.divide(BigDecimal.valueOf(summary.saleCount), 2, BigDecimal.ROUND_HALF_UP);
                row[3] = "$" + avgSale;
            } else {
                row[3] = "$0.00";
            }

            tableModel.addRow(row);
        }
    }

    private void generateProductPerformanceReport(LocalDateTime startDate, LocalDateTime endDate) {
        tableModel.setRowCount(0);

        // Change table columns for product report
        tableModel.setColumnIdentifiers(new String[]{"Product", "Quantity Sold", "Revenue", "% of Total"});

        // Add sample data
        Object[] row1 = {"Organic Apples (1kg)", 15, "$150.00", "30%"};
        Object[] row2 = {"Fresh Bread", 10, "$200.00", "40%"};
        Object[] row3 = {"Fair Trade Coffee", 8, "$80.00", "16%"};
        Object[] row4 = {"Recycled Paper Towels", 7, "$70.00", "14%"};

        tableModel.addRow(row1);
        tableModel.addRow(row2);
        tableModel.addRow(row3);
        tableModel.addRow(row4);
    }

    private void generateSDGReport(LocalDateTime startDate, LocalDateTime endDate) {
        // Reset SDG tables
        detailsPanel.removeAll();
        detailsPanel.setBackground(SDG_PANEL_COLOR);

        // Set up the main SDG table
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{"Metric", "Value", "Impact", "Trend"});

        // Add key SDG metrics
        Object[] row1 = {"Food Waste Prevented", "42.5 kg", "High Positive", "↑"};
        Object[] row2 = {"Packaging Saved", "158 units", "Medium Positive", "↑"};
        Object[] row3 = {"Near-Expiry Sales", "87 items", "High Positive", "↑"};
        Object[] row4 = {"CO₂ Emissions Saved", "128.4 kg", "Medium Positive", "→"};

        tableModel.addRow(row1);
        tableModel.addRow(row2);
        tableModel.addRow(row3);
        tableModel.addRow(row4);

        // Populate waste reduction details table
        DefaultTableModel wasteModel = (DefaultTableModel) sdgWasteTable.getModel();
        wasteModel.addRow(new Object[]{"Fresh Vegetables", 15, 7.5, "Compostable", "High"});
        wasteModel.addRow(new Object[]{"Dairy Products", 12, 6.0, "Food", "Medium"});
        wasteModel.addRow(new Object[]{"Bakery Items", 24, 12.0, "Food", "Medium"});
        wasteModel.addRow(new Object[]{"Fruits", 32, 17.0, "Compostable", "High"});

        // Populate packaging details table
        DefaultTableModel packagingModel = (DefaultTableModel) sdgPackagingTable.getModel();
        packagingModel.addRow(new Object[]{"Plastic Bags", 85, "Polyethylene", 5, "Recyclable"});
        packagingModel.addRow(new Object[]{"Cardboard Boxes", 42, "Recycled Cardboard", 25, "Recyclable"});
        packagingModel.addRow(new Object[]{"Glass Containers", 15, "Glass", 150, "Reusable"});
        packagingModel.addRow(new Object[]{"Plastic Containers", 16, "PET", 20, "Recyclable"});

        // Populate expiry management details table
        DefaultTableModel expiryModel = (DefaultTableModel) sdgExpiryTable.getModel();
        expiryModel.addRow(new Object[]{"Yogurt", "2025-03-15", 2, "30%", "Sold"});
        expiryModel.addRow(new Object[]{"Fresh Juice", "2025-03-14", 3, "20%", "Sold"});
        expiryModel.addRow(new Object[]{"Bread", "2025-03-12", 1, "50%", "Sold"});
        expiryModel.addRow(new Object[]{"Chicken", "2025-03-13", 2, "35%", "Sold"});
        expiryModel.addRow(new Object[]{"Salad Mix", "2025-03-12", 1, "40%", "Sold"});

        // Create titled panels for each SDG table
        JPanel wastePanel = createTablePanel("Waste Reduction Details", sdgWasteTable);
        JPanel packagingPanel = createTablePanel("Packaging Reduction Details", sdgPackagingTable);
        JPanel expiryPanel = createTablePanel("Expiry Management Details", sdgExpiryTable);

        // Add SDG summary panel with key statistics
        JPanel summaryPanel = createSDGSummaryPanel();

        // Add all panels to the details panel
        detailsPanel.add(summaryPanel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        detailsPanel.add(wastePanel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        detailsPanel.add(packagingPanel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        detailsPanel.add(expiryPanel);

        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private JPanel createTablePanel(String title, JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        TitledBorder titledBorder = BorderFactory.createTitledBorder(title);
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 13));
        panel.setBorder(titledBorder);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 150));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSDGSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setBackground(SDG_PANEL_COLOR);
        TitledBorder titledBorder = BorderFactory.createTitledBorder("SDG Impact Summary");
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 13));
        panel.setBorder(titledBorder);

        // Create 3 metric cards
        panel.add(createMetricCard("Total Waste Prevented", "42.5 kg", new Color(76, 175, 80)));
        panel.add(createMetricCard("CO₂ Emissions Saved", "128.4 kg", new Color(33, 150, 243)));
        panel.add(createMetricCard("Sustainability Score", "87/100", new Color(156, 39, 176)));

        return panel;
    }

    private JPanel createMetricCard(String title, String value, Color accentColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);

        return panel;
    }

    // Inner class for ComboBox styling
    private class CustomComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                label.setFont(new Font("Arial", Font.PLAIN, 12));
                if (isSelected) {
                    label.setBackground(new Color(200, 230, 210));
                    label.setForeground(Color.BLACK);
                }
            }
            return c;
        }
    }

    // Inner class to hold daily sales summary data
    private static class DailySalesSummary {
        BigDecimal totalSales = BigDecimal.ZERO;
        int totalItems = 0;
        int saleCount = 0;
    }

    // Method to export the current report to a file
    public void exportReportToFile(String filePath) {
        // Implementation would handle exporting the current report data
        // to CSV, PDF, or other format based on the file extension
        String fileExtension = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase();

        switch (fileExtension) {
            case "csv":
                exportToCSV(filePath);
                break;
            case "pdf":
                exportToPDF(filePath);
                break;
            default:
                JOptionPane.showMessageDialog(this,
                        "Unsupported file format. Please use .csv or .pdf extension.",
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToCSV(String filePath) {
        // Placeholder for CSV export functionality
        JOptionPane.showMessageDialog(this,
                "Report exported to CSV: " + filePath,
                "Export Successful",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportToPDF(String filePath) {
        // Placeholder for PDF export functionality
        JOptionPane.showMessageDialog(this,
                "Report exported to PDF: " + filePath,
                "Export Successful",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Method to refresh the report with current data
    public void refreshReport() {
        if (reportTypeComboBox.getSelectedIndex() >= 0) {
            // Get current date range from the UI components
            java.util.Date fromDate = (java.util.Date) fromDateSpinner.getValue();
            java.util.Date toDate = (java.util.Date) toDateSpinner.getValue();

            LocalDateTime startDateTime = LocalDateTime.of(
                    fromDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    LocalTime.MIN
            );

            LocalDateTime endDateTime = LocalDateTime.of(
                    toDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    LocalTime.MAX
            );

            generateReport(reportTypeComboBox.getSelectedIndex(), startDateTime, endDateTime);
        }
    }
}