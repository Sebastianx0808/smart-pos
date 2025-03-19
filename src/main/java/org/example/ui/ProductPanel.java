package org.example.ui;

import org.example.model.Product;
import org.example.service.ProductService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


public class ProductPanel extends JPanel {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private ProductService productService;

    private JTextField nameField;
    private JTextField priceField;
    private JTextField stockField;
    private JTextField expiryField;
    private JTextField categoryField;

    public ProductPanel() {
        productService = new ProductService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table
        createTable();

        // Create form panel
        JPanel formPanel = createFormPanel();

        // Add components
        add(new JScrollPane(productTable), BorderLayout.CENTER);
        add(formPanel, BorderLayout.EAST);

        // SDG Feature: Add alert panel
        JPanel alertPanel = createAlertPanel();
        add(alertPanel, BorderLayout.NORTH);

        // Load data
        refreshProductTable();
    }

    private void createTable() {
        String[] columnNames = {"ID", "Name", "Price", "Stock", "Expiry Date", "Category"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        productTable = new JTable(tableModel);
        productTable.setRowHeight(25);
        productTable.setSelectionBackground(new Color(173, 216, 230));
        productTable.setSelectionForeground(Color.BLACK);
        productTable.setGridColor(new Color(211, 211, 211));
        productTable.setFont(new Font("Arial", Font.PLAIN, 12));
        productTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        productTable.getTableHeader().setBackground(new Color(70, 130, 180));
        productTable.getTableHeader().setForeground(Color.BLACK);
        productTable.setShowGrid(true);
        productTable.setAutoCreateRowSorter(true);

        //Custom renderer for highlighting low stock and expiring products
        productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    int modelRow = table.convertRowIndexToModel(row);
                    int stock = (int) tableModel.getValueAt(modelRow, 3);
                    String expiryDateStr = tableModel.getValueAt(modelRow, 4).toString();

                    if (stock < 10) {
                        c.setBackground(new Color(255, 240, 240)); // Light red for low stock
                    } else if (!expiryDateStr.isEmpty() &&
                            LocalDate.parse(expiryDateStr).isBefore(LocalDate.now().plusDays(30))) {
                        c.setBackground(new Color(255, 248, 220)); // Light yellow for expiring
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }

                return c;
            }
        });

        // Selection listener
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = productTable.getSelectedRow();
                if (selectedRow != -1) {
                    populateFormFields(selectedRow);
                }
            }
        });
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2), "Product Details",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), new Color(70, 130, 180)));
        panel.setPreferredSize(new Dimension(320, 450));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        //Form fields
        JLabel[] labels = {
                new JLabel("Name:"), new JLabel("Price:"), new JLabel("Stock:"),
                new JLabel("Expiry Date:"), new JLabel("Category:")
        };

        for (JLabel label : labels) {
            label.setFont(new Font("Arial", Font.BOLD, 12));
        }

        // Form fields
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(labels[0], gbc);

        gbc.gridx = 1;
        nameField = new JTextField(20);
        nameField.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(labels[1], gbc);

        gbc.gridx = 1;
        priceField = new JTextField(20);
        priceField.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(priceField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(labels[2], gbc);

        gbc.gridx = 1;
        stockField = new JTextField(20);
        stockField.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(stockField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(labels[3], gbc);

        gbc.gridx = 1;
        JPanel datePanel = new JPanel(new BorderLayout());
        expiryField = new JTextField(20);
        expiryField.setFont(new Font("Arial", Font.PLAIN, 12));
        expiryField.setToolTipText("Format: YYYY-MM-DD");

        JButton calendarButton = new JButton("...");
        calendarButton.setPreferredSize(new Dimension(30, 25));
        calendarButton.addActionListener(e -> showDatePicker());
        datePanel.add(expiryField, BorderLayout.CENTER);
        datePanel.add(calendarButton, BorderLayout.EAST);
        panel.add(datePanel, gbc);


        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(labels[4], gbc);

        gbc.gridx = 1;
        categoryField = new JTextField(20);
        categoryField.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(categoryField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        JLabel statusLabel = new JLabel("");
        statusLabel.setForeground(Color.BLUE);
        panel.add(statusLabel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));

        JButton newButton = createStyledButton("New", new Color(70, 130, 180));
        JButton saveButton = createStyledButton("Save", new Color(46, 139, 87));
        JButton deleteButton = createStyledButton("Delete", new Color(178, 34, 34));

        newButton.addActionListener(e -> clearFormFields());
        saveButton.addActionListener(e -> {
            if (saveProduct()) {
                statusLabel.setText("Operation successful");
                statusLabel.setForeground(new Color(46, 139, 87));
            } else {
                statusLabel.setText("Operation failed");
                statusLabel.setForeground(Color.RED);
            }
        });

        deleteButton.addActionListener(e -> {
            if (deleteProduct()) {
                statusLabel.setText("Product deleted successfully");
                statusLabel.setForeground(new Color(46, 139, 87));
            }
        });

        buttonPanel.add(newButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private void showDatePicker() {
        // Simple date picker implementation
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Select Date:");
        panel.add(label);

        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1));
        JSpinner monthSpinner = new JSpinner(new SpinnerNumberModel(LocalDate.now().getMonthValue(), 1, 12, 1));
        JSpinner daySpinner = new JSpinner(new SpinnerNumberModel(LocalDate.now().getDayOfMonth(), 1, 31, 1));

        JPanel datePanel = new JPanel(new FlowLayout());
        datePanel.add(new JLabel("Year:"));
        datePanel.add(yearSpinner);
        datePanel.add(new JLabel("Month:"));
        datePanel.add(monthSpinner);
        datePanel.add(new JLabel("Day:"));
        datePanel.add(daySpinner);

        panel.add(datePanel);

        int result = JOptionPane.showConfirmDialog(null, panel, "Date Picker", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int year = (int) yearSpinner.getValue();
            int month = (int) monthSpinner.getValue();
            int day = (int) daySpinner.getValue();

            // Validate date
            try {
                LocalDate date = LocalDate.of(year, month, day);
                expiryField.setText(date.toString());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Invalid date!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        return button;
    }

    private JPanel createAlertPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(255, 140, 0), 2), "Alerts",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), new Color(255, 140, 0)));

        JTabbedPane alertTabs = new JTabbedPane();

        // Low stock products tab
        JList<Product> lowStockList = new JList<>();
        DefaultListModel<Product> lowStockModel = new DefaultListModel<>();
        lowStockList.setModel(lowStockModel);
        lowStockList.setCellRenderer(new ProductListCellRenderer());

        // Expiring products tab
        JList<Product> expiringList = new JList<>();
        DefaultListModel<Product> expiringModel = new DefaultListModel<>();
        expiringList.setModel(expiringModel);
        expiringList.setCellRenderer(new ProductListCellRenderer());

        // Populate lists
        List<Product> lowStockProducts = productService.getLowStockProducts();
        for (Product p : lowStockProducts) {
            lowStockModel.addElement(p);
        }

        List<Product> expiringProducts = productService.getExpiringProducts();
        for (Product p : expiringProducts) {
            expiringModel.addElement(p);
        }

        // Add tabs
        alertTabs.addTab("Low Stock (" + lowStockProducts.size() + ")", new JScrollPane(lowStockList));
        alertTabs.setForegroundAt(0, Color.RED);

        alertTabs.addTab("Expiring (" + expiringProducts.size() + ")", new JScrollPane(expiringList));
        alertTabs.setForegroundAt(1, new Color(255, 140, 0));

        panel.add(alertTabs, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        // Add refresh button
        JButton refreshButton = new JButton("Refresh Alerts");
        refreshButton.addActionListener(e -> refreshAlerts(lowStockModel, expiringModel));

        JButton restockButton = new JButton("Order for Restock");
        restockButton.setBackground(new Color(70, 130, 180));
        restockButton.setForeground(Color.WHITE);
        restockButton.setFont(new Font("Arial", Font.BOLD, 12));
        restockButton.setFocusPainted(false);
        restockButton.setBorderPainted(false);
        restockButton.setOpaque(true);
        restockButton.addActionListener(e -> showRestockDialog(lowStockProducts));
        buttonPanel.add(refreshButton);
        buttonPanel.add(restockButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showRestockDialog(List<Product> lowStockProducts){
        if (lowStockProducts.isEmpty()){
            JOptionPane.showMessageDialog(this, "No products need restocking.", "Restock Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog restockDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Order for Restock", true);
        restockDialog.setLayout(new BorderLayout());
        restockDialog.setSize(600, 400);
        restockDialog.setLocationRelativeTo(this);

        String[] columnNames = {"ID", "Name", "Current Stock", "Order Quantity", "New Expiry Date"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4; // Only allow editing Order Quantity and New Expiry Date
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return Integer.class;
                return Object.class;
            }
        };

        JTable restockTable = new JTable(model);
        restockTable.setRowHeight(25);
        restockTable.setSelectionBackground(new Color(173, 216, 230));

        // Add products to the table
        for (Product product : lowStockProducts) {
            Object[] row = new Object[5];
            row[0] = product.getId();
            row[1] = product.getName();
            row[2] = product.getStock();
            row[3] = 10; // Default order quantity suggestion

            // Calculate default expiry date (6 months from now)
            LocalDate defaultExpiryDate = LocalDate.now().plusMonths(6);
            row[4] = defaultExpiryDate.toString();

            model.addRow(row);
        }

        //date editor for expiry date column
        restockTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JTextField()) {
            private JTextField field;

            {
                field = (JTextField) getComponent();
                field.addActionListener(e -> stopCellEditing());
            }

            @Override
            public boolean stopCellEditing() {
                String value = field.getText();
                try {
                    if (!value.isEmpty()) {
                        LocalDate.parse(value);
                    }
                    return super.stopCellEditing();
                } catch (DateTimeParseException e) {
                    JOptionPane.showMessageDialog(null, "Invalid date format. Please use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        });

        //scroll pane with table to dialog
        JScrollPane scrollPane = new JScrollPane(restockTable);
        restockDialog.add(scrollPane, BorderLayout.CENTER);

        // Instructions panel
        JPanel instructionPanel = new JPanel();
        instructionPanel.setLayout(new BoxLayout(instructionPanel, BoxLayout.Y_AXIS));
        instructionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel instructionLabel = new JLabel("Enter the quantity to order and the new expiry date for each product.");
        instructionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        instructionPanel.add(instructionLabel);

        JLabel dateFormatLabel = new JLabel("Date format: YYYY-MM-DD");
        dateFormatLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateFormatLabel.setForeground(Color.GRAY);
        instructionPanel.add(dateFormatLabel);

        restockDialog.add(instructionPanel, BorderLayout.NORTH);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> restockDialog.dispose());

        JButton orderButton = new JButton("Place Order");
        orderButton.setBackground(new Color(46, 139, 87));
        orderButton.setForeground(Color.WHITE);
        orderButton.setFont(new Font("Arial", Font.BOLD, 12));
        orderButton.setFocusPainted(false);
        orderButton.setBorderPainted(false);
        orderButton.setOpaque(true);

        orderButton.addActionListener(e -> {
            boolean success = processRestockOrder(model);
            if (success) {
                JOptionPane.showMessageDialog(restockDialog, "Order placed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                restockDialog.dispose();
                refreshProductTable();
                refreshAlerts();
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(orderButton);
        restockDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Show dialog
        restockDialog.setVisible(true);


    }

    private boolean processRestockOrder(DefaultTableModel model){
        try{
            for (int i=0; i<model.getRowCount(); i++){
                int productId = (int) model.getValueAt(i,0);
                int currentStock = (int) model.getValueAt(i, 2);
                int orderQuantity = (int) model.getValueAt(i, 3);
                String expiryDateStr = model.getValueAt(i, 4).toString();

                if (orderQuantity <= 0) {
                    JOptionPane.showMessageDialog(this, "Order quantity must be greater than 0 for product ID: " + productId,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                LocalDate expiryDate = null;
                if (!expiryDateStr.isEmpty()) {
                    try {
                        expiryDate = LocalDate.parse(expiryDateStr);
                        if (expiryDate.isBefore(LocalDate.now())) {
                            JOptionPane.showMessageDialog(this, "Expiry date must be in the future for product ID: " + productId,
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    } catch (DateTimeParseException e) {
                        JOptionPane.showMessageDialog(this, "Invalid date format for product ID: " + productId,
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }

                //this function is to get the product from database
                Product product = productService.getProductById(productId);
                if (product == null){
                    JOptionPane.showMessageDialog(this, "Product not found: " + productId, "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                int newStock = currentStock + orderQuantity;
                product.setStock(newStock);
                product.setExpiryDate(expiryDate);

                boolean updateSuccess = productService.updateProduct(product.getId(), product.getName(), product.getPrice(),
                        newStock, expiryDate, product.getCategory());
                if (!updateSuccess) {
                    JOptionPane.showMessageDialog(this, "Failed to update product: " + product.getName(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            return true;
        } catch (Exception ex){
            JOptionPane.showMessageDialog(this, "Error processing order: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;
        }
    }

    // Custom renderer for product lists
    class ProductListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Product) {
                Product product = (Product) value;
                setText(product.getName() + " - Stock: " + product.getStock() +
                        (product.getExpiryDate() != null ? " - Expires: " + product.getExpiryDate() : ""));

                if (product.isLowStock()) {
                    setForeground(Color.RED);
                }
                if (product.isExpiring()) {
                    setFont(getFont().deriveFont(Font.BOLD));
                }
            }

            return this;
        }
    }

    private void refreshAlerts(DefaultListModel<Product> lowStockModel, DefaultListModel<Product> expiringModel) {
        lowStockModel.clear();
        expiringModel.clear();

        List<Product> lowStockProducts = productService.getLowStockProducts();
        for (Product p : lowStockProducts) {
            lowStockModel.addElement(p);
        }

        List<Product> expiringProducts = productService.getExpiringProducts();
        for (Product p : expiringProducts) {
            expiringModel.addElement(p);
        }
    }

    private void refreshProductTable() {
        tableModel.setRowCount(0);

        List<Product> products = productService.getAllProducts();

        for (Product product : products) {
            Object[] row = new Object[6];
            row[0] = product.getId();
            row[1] = product.getName();
            row[2] = product.getPrice();
            row[3] = product.getStock();
            row[4] = product.getExpiryDate() != null ? product.getExpiryDate().toString() : "";
            row[5] = product.getCategory();

            tableModel.addRow(row);
        }
    }

    private void populateFormFields(int selectedRow) {
        nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
        priceField.setText(tableModel.getValueAt(selectedRow, 2).toString());
        stockField.setText(tableModel.getValueAt(selectedRow, 3).toString());
        expiryField.setText(tableModel.getValueAt(selectedRow, 4) != null ? tableModel.getValueAt(selectedRow, 4).toString() : "");
        categoryField.setText(tableModel.getValueAt(selectedRow, 5) != null ? tableModel.getValueAt(selectedRow, 5).toString() : "");
    }

    private void clearFormFields() {
        nameField.setText("");
        priceField.setText("");
        stockField.setText("");
        expiryField.setText("");
        categoryField.setText("");
        productTable.clearSelection();
    }

    private boolean saveProduct() {
        try {
            String name = nameField.getText().trim();
            BigDecimal price = new BigDecimal(priceField.getText().trim());
            int stock = Integer.parseInt(stockField.getText().trim());

            LocalDate expiryDate = null;
            if (!expiryField.getText().trim().isEmpty()) {
                try {
                    expiryDate = LocalDate.parse(expiryField.getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (DateTimeParseException e) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }

            String category = categoryField.getText().trim();

            int selectedRow = productTable.getSelectedRow();
            boolean success;

            if (selectedRow == -1) {
                // Create new product
                success = productService.createProduct(name, price, stock, expiryDate, category);
            } else {
                // Update existing product
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                success = productService.updateProduct(id, name, price, stock, expiryDate, category);
            }

            if (success) {
                clearFormFields();
                refreshProductTable();
                refreshAlerts(); // Refresh alerts after modification
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Add this method to refresh alerts after product changes
    private void refreshAlerts() {
        // Remove the old alert panel
        Component[] components = getComponents();
        for (Component component : components) {
            if (component instanceof JPanel && ((JPanel) component).getBorder() instanceof TitledBorder) {
                TitledBorder border = (TitledBorder) ((JPanel) component).getBorder();
                if (border.getTitle().equals("Alerts")) {
                    remove(component);
                    break;
                }
            }
        }

        // Add a new alert panel
        JPanel alertPanel = createAlertPanel();
        add(alertPanel, BorderLayout.NORTH);

        revalidate();
        repaint();
    }

    private boolean deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this product?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            boolean success = productService.deleteProduct(id);

            if (success) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully.");
                clearFormFields();
                refreshProductTable();
                refreshAlerts();
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Deletion failed.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }
}