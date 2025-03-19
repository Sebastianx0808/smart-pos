package org.example.ui;

import org.example.model.Product;
import org.example.model.Sale;
import org.example.service.ProductService;
import org.example.service.SaleService;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SalesPanel extends JPanel {
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private JComboBox<Product> productComboBox;
    private JComboBox<String> paymentMethodComboBox;
    private JTextField searchField;
    private JSpinner quantitySpinner;
    private JLabel totalLabel;
    private JTextField discountField;
    private JComboBox<String> discountTypeComboBox;

    private SaleService saleService;
    private ProductService productService;
    private Sale currentSale;
    private List<Product> allProducts;

    // Shop constants for receipt
    private static final String SHOP_NAME = "Chakkappan Stores";
    private static final String SHOP_ADDRESS = "SG Palya, Taverekkare, Bengaluru, 560029";
    private static final String SHOP_PHONE = "+91 7356711236";
    private static final String SHOP_EMAIL = "info@chakkppanstore.com";
    private static final String SHOP_WEBSITE = "www.chakkappanstores.com";
    private static final String RECEIPT_FOOTER = "Thank you for shopping with us!";
    private boolean includeLogoOnReceipt = true;
    private boolean includeFooterMessage = true;
    private String customReceiptMessage = "";

    public SalesPanel() {
        saleService = new SaleService();
        productService = new ProductService();
        allProducts = productService.getAllProducts();
        currentSale = saleService.createNewSale();



        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));



        // Create components
        JPanel topPanel = createTopPanel();
        JPanel centerPanel = createCenterPanel();
        JPanel bottomPanel = createBottomPanel();

        // Add components
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        refreshPanel();
    }

    private void styleButton(JButton button, Color bgColor, Color fgColor){
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setFont(new Font(button.getFont().getName(), Font.BOLD, button.getFont().getSize()));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
    }

    private TitledBorder createStyledTitledBorder(String title) {
        // Create a custom line border with rounded corners
        Border lineBorder = BorderFactory.createLineBorder(new Color(70, 130, 180), 2, true);

        // Create titled border with the line border
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                lineBorder,
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP);

        // Set the title font and color
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        titledBorder.setTitleColor(new Color(25, 25, 112));

        // Add some padding around the content
        Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        Border compoundBorder = BorderFactory.createCompoundBorder(titledBorder, emptyBorder);

        return titledBorder;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(createStyledTitledBorder("Add Products"));
        panel.setBackground(new Color(240, 248, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Search field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Search:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        searchField = new JTextField(20);
        panel.add(searchField, gbc);

        // Add document listener for real-time filtering
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterProducts();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterProducts();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterProducts();
            }
        });

        // Product dropdown
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Product:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        productComboBox = new JComboBox<>();
        productComboBox.setRenderer(new ProductListCellRenderer());
        panel.add(productComboBox, gbc);

        // Initially populate with all products
        populateProductComboBox(allProducts);

        // Quantity spinner
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Quantity:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 100, 1);
        quantitySpinner = new JSpinner(spinnerModel);
        panel.add(quantitySpinner, gbc);

        // Add button
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        JButton addButton = new JButton("Add to Cart");
        styleButton(addButton, new Color(60, 179, 113), Color.BLACK);
        addButton.addActionListener(e -> addToCart());
        panel.add(addButton, gbc);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(createStyledTitledBorder("Shopping Cart"));
        panel.setBackground(new Color(240, 248, 255));

        // Create cart table
        String[] columnNames = {"Product", "Price", "Quantity", "Subtotal"};
        cartTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        cartTable = new JTable(cartTableModel);

        // Add table to panel
        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        setupCartTableContextMenu();


        JPanel cartButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        // Add remove button
        JButton removeButton = new JButton("Remove Selected");
        styleButton(removeButton, new Color(220, 53, 69), Color.BLACK);
        removeButton.setToolTipText("Remove selected item from cart (Delete)");
        removeButton.addActionListener(e -> removeFromCart());

        JButton editButton = new JButton("Edit Selected");
        styleButton(editButton, new Color(23, 162, 184), Color.BLACK);
        editButton.setToolTipText("Edit the selected product");
        editButton.addActionListener(e -> editCartItem());

        JButton clearButton = new JButton("Clear Cart");
        styleButton(clearButton, new Color(108, 117, 125), Color.BLACK);
        clearButton.addActionListener(e -> clearCart());

        cartButtonsPanel.add(removeButton);
        cartButtonsPanel.add(editButton);
        cartButtonsPanel.add(clearButton);

        panel.add(cartButtonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Total panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JPanel discountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        discountPanel.add(new JLabel("Discount:"));
        discountField = new JTextField("0", 5);
        discountPanel.add(discountField);
        discountTypeComboBox = new JComboBox<>(new String[]{"Percent (%)", "Amount ($)"});
        discountPanel.add(discountTypeComboBox);
        JButton applyDiscountButton = new JButton("Apply");
        applyDiscountButton.addActionListener(e -> applyDiscount());
        discountPanel.add(applyDiscountButton);

        //payment method combobox
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        paymentPanel.add(new JLabel("Payment Method:"));
        paymentMethodComboBox = new JComboBox<>(new String[]{"Cash", "Credit Card", "Debit Card", "UPI"});
        paymentPanel.add(paymentMethodComboBox);

        totalPanel.add(new JLabel("Total:"));
        totalLabel = new JLabel("₹ 0.00");
        totalLabel.setFont(new Font(totalLabel.getFont().getName(), Font.BOLD, 16));
        totalPanel.add(totalLabel);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton newSaleButton = new JButton("New Sale");
        styleButton(newSaleButton, new Color(108, 117, 125), Color.BLACK);

        JButton checkoutButton = new JButton("Checkout");
        styleButton(checkoutButton, new Color(0, 123, 255), Color.BLACK);

        JButton printReceiptButton = new JButton("Print Receipt");
        styleButton(printReceiptButton, new Color(255, 193, 7), Color.BLACK);

        JButton customizedReceiptButton = new JButton("Customize Receipt");
        styleButton(customizedReceiptButton, new Color(108, 117, 125), Color.BLACK);

        JButton refreshButton = new JButton("Refresh");
        styleButton(refreshButton, new Color(23, 162, 184), Color.BLACK);
        refreshButton.setToolTipText("Refresh product list and panel");
        refreshButton.addActionListener(e -> refreshPanel());
        buttonsPanel.add(refreshButton);


        newSaleButton.addActionListener(e -> newSale());
        checkoutButton.addActionListener(e -> checkout());
        printReceiptButton.addActionListener(e -> printReceipt());
        customizedReceiptButton.addActionListener(e -> showReceiptCustomizationDialog());
        printReceiptButton.setEnabled(false); // Disabled until checkout

        buttonsPanel.add(newSaleButton);
        buttonsPanel.add(checkoutButton);
        buttonsPanel.add(customizedReceiptButton,2);
        buttonsPanel.add(printReceiptButton);

        // Combine panels
        panel.add(paymentPanel, BorderLayout.WEST);
        panel.add(totalPanel, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        panel.add(discountPanel, BorderLayout.NORTH);

        return panel;
    }

    private void applyDiscount() {
        try {
            double discountValue = Double.parseDouble(discountField.getText().trim());
            String discountType = (String) discountTypeComboBox.getSelectedItem();

            if (discountValue < 0) {
                JOptionPane.showMessageDialog(this, "Discount cannot be negative!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (discountType.startsWith("Percent") && discountValue > 100) {
                JOptionPane.showMessageDialog(this, "Percentage discount cannot exceed 100%!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal originalTotal = currentSale.calculateRawTotal();
            double discountAmount;

            if (discountType.startsWith("Percent")) {
                discountAmount = originalTotal.multiply(
                        BigDecimal.valueOf(discountValue / 100.0)).doubleValue();
            } else {
                discountAmount = discountValue;
                if (discountAmount > originalTotal.doubleValue()) {
                    JOptionPane.showMessageDialog(this, "Discount amount cannot exceed total!",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            currentSale.setDiscountAmount(discountAmount);
            currentSale.setDiscountType(discountType);
            updateCartTable();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterProducts() {
        String searchText = searchField.getText().trim().toLowerCase();

        // Filter products based on search text using streams
        List<Product> filteredProducts;

        if (searchText.isEmpty()) {
            filteredProducts = allProducts; // Show all products when search is empty
        } else {
            filteredProducts = allProducts.stream()
                    .filter(product ->
                            product.getName().toLowerCase().contains(searchText) ||
                                    (product.getDescription() != null &&
                                            product.getDescription().toLowerCase().contains(searchText)))
                    .collect(Collectors.toList());
        }

        // Update the combo box with filtered products
        populateProductComboBox(filteredProducts);
    }

    private void populateProductComboBox(List<Product> products) {
        // Save selected item to restore after refresh if possible
        Product selectedProduct = (Product) productComboBox.getSelectedItem();

        productComboBox.removeAllItems();

        for (Product product : products) {
            productComboBox.addItem(product);
        }

        // Try to restore previous selection
        if (selectedProduct != null && products.contains(selectedProduct)) {
            productComboBox.setSelectedItem(selectedProduct);
        } else if (!products.isEmpty()) {
            productComboBox.setSelectedIndex(0);
        }
    }

    // Original populateProductComboBox now simply delegates to the new one
    private void populateProductComboBox() {
        populateProductComboBox(allProducts);
    }

    private void setupCartTableContextMenu(){
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("Edit Item");
        editItem.addActionListener(e -> editCartItem());

        JMenuItem removeItem = new JMenuItem("Remove Item");
        removeItem.addActionListener(e -> removeFromCart());

        JMenuItem clearCartItem = new JMenuItem("Clear Cart");
        clearCartItem.addActionListener(e -> clearCart());

        popupMenu.add(editItem);
        popupMenu.add(removeItem);
        popupMenu.addSeparator();
        popupMenu.add(clearCartItem);

        cartTable.setComponentPopupMenu(popupMenu);
    }
    private void addToCart() {
        Product selectedProduct = (Product) productComboBox.getSelectedItem();
        int quantity = (int) quantitySpinner.getValue();

        if (selectedProduct != null && quantity > 0) {
            if (selectedProduct.getStock() < quantity) {
                JOptionPane.showMessageDialog(this, "Not enough stock available!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            saleService.addItemToSale(currentSale, selectedProduct, quantity);
            updateCartTable();

            // Reset quantity spinner to 1 after adding product
            quantitySpinner.setValue(1);

            // Clear search field and show all products again
            searchField.setText("");
            populateProductComboBox(allProducts);
        }
    }

    private void removeFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow != -1 && selectedRow < currentSale.getItems().size()) {
            currentSale.getItems().remove(selectedRow);
            updateCartTable();
        }
    }

    private void clearCart(){
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear the cart?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if(confirm == JOptionPane.YES_OPTION){
          currentSale.getItems().clear();
          updateCartTable();
        }
    }

    private void editCartItem(){
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow !=-1 && selectedRow < currentSale.getItems().size()) {
            Sale.SaleItem item = currentSale.getItems().get(selectedRow);

            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
                    item.getQuantity(), 1, 100, 1);
            JSpinner quantitySpinner = new JSpinner(spinnerModel);

            JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
            panel.add(new JLabel("Product:"));
            panel.add(new JLabel(item.getProductName()));
            panel.add(new JLabel("Quantity:"));
            panel.add(quantitySpinner);

            int result = JOptionPane.showConfirmDialog(this, panel,
                    "Edit Item", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                int newQuantity = (int) quantitySpinner.getValue();

                // Check stock before updating
                Product product = productService.getProductById(item.getProductId());
                if (product != null && product.getStock() < newQuantity) {
                    JOptionPane.showMessageDialog(this,
                            "Not enough stock available!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                item.setQuantity(newQuantity);
                updateCartTable();
            }


        }
    }

    private void updateCartTable() {
        cartTableModel.setRowCount(0);

        for (Sale.SaleItem item : currentSale.getItems()) {
            Object[] row = new Object[4];
            row[0] = item.getProductName();
            row[1] = "$" + item.getUnitPrice();
            row[2] = item.getQuantity();
            row[3] = "$" + item.getSubtotal();

            cartTableModel.addRow(row);
        }

        totalLabel.setText("$" + currentSale.getTotalAmount());
    }

    private void newSale() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to start a new sale?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            currentSale = saleService.createNewSale();
            updateCartTable();

            // Disable print receipt button for new sale
            JButton printReceiptButton = findPrintReceiptButton();
            if (printReceiptButton != null) {
                printReceiptButton.setEnabled(false);
            }

            // Clear search field and reset products display
            searchField.setText("");
            populateProductComboBox(allProducts);
        }
    }

    private void checkout() {
        if (currentSale.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String selectedPaymentMethod = (String) paymentMethodComboBox.getSelectedItem();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm checkout?\nTotal: ₹" + currentSale.getTotalAmount() +
                        "\nPayment Method: " + selectedPaymentMethod,
                "Checkout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean paymentReceived = confirmPayment(selectedPaymentMethod);

            if (!paymentReceived) {
                return; // Cancel checkout if payment not received
            }

            currentSale.setPaymentMethod(selectedPaymentMethod);

            boolean success = saleService.processSale(currentSale);

            if (success) {
                JOptionPane.showMessageDialog(this, "Sale completed successfully!");

                // Enable print receipt button
                JButton printReceiptButton = findPrintReceiptButton();
                if (printReceiptButton != null) {
                    printReceiptButton.setEnabled(true);
                }

                // Offer to print receipt
                int printConfirm = JOptionPane.showConfirmDialog(this,
                        "Would you like to print the receipt?",
                        "Print Receipt",
                        JOptionPane.YES_NO_OPTION);

                if (printConfirm == JOptionPane.YES_OPTION) {
                    printReceipt();
                }

                // Create new sale after printing option
                currentSale = saleService.createNewSale();
                updateCartTable();

                // Refresh product list to reflect updated stock
                allProducts = productService.getAllProducts();
                searchField.setText("");
                populateProductComboBox(allProducts);
            } else {
                JOptionPane.showMessageDialog(this, "Sale processing failed!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private boolean confirmPayment(String paymentMethod) {
        String message = "Has the payment of $" + currentSale.getTotalAmount() +
                " been received via " + paymentMethod + "?";

        int response = JOptionPane.showConfirmDialog(this, message,
                "Payment Confirmation", JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.NO_OPTION) {
            JOptionPane.showMessageDialog(this,
                    "Please collect payment before proceeding.",
                    "Payment Required", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private JButton findPrintReceiptButton() {
        Component[] components = ((JPanel)getComponent(2)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                Component[] subComps = ((JPanel)comp).getComponents();
                for (Component subComp : subComps) {
                    if (subComp instanceof JButton && ((JButton)subComp).getText().equals("Print Receipt")) {
                        return (JButton)subComp;
                    }
                }
            }
        }
        return null;
    }

    private void printReceipt() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new ReceiptPrintable(currentSale));

        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this, "Receipt printed successfully!");
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Failed to print receipt: " + ex.getMessage(),
                        "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showReceiptCustomizationDialog(){
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Receipt Customization", true);
        dialog.setLayout(new BorderLayout());

        JPanel optionsPanel = new JPanel(new GridLayout(3, 1));

        JCheckBox logoCheckBox = new JCheckBox("Include Shop Logo", includeLogoOnReceipt);
        JCheckBox footerCheckBox = new JCheckBox("Include Footer Message", includeFooterMessage);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(new JLabel("Custom Message:"), BorderLayout.NORTH);
        JTextField messageField = new JTextField(customReceiptMessage, 30);
        messagePanel.add(messageField, BorderLayout.CENTER);

        optionsPanel.add(logoCheckBox);
        optionsPanel.add(footerCheckBox);
        optionsPanel.add(messagePanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            includeLogoOnReceipt = logoCheckBox.isSelected();
            includeFooterMessage = footerCheckBox.isSelected();
            customReceiptMessage = messageField.getText();
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(optionsPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Custom renderer for the product combo box to show more details
    private class ProductListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Product) {
                Product product = (Product) value;
                setText(product.getName() + " - $" + product.getPrice() + " (Stock: " + product.getStock() + ")");
            }

            return this;
        }
    }

    private void refreshPanel(){
        allProducts = productService.getAllProducts();
        searchField.setText("");
        populateProductComboBox(allProducts);
        if (currentSale == null || currentSale.getItems().isEmpty()) {
            currentSale = saleService.createNewSale();
        }

        updateCartTable();

        discountField.setText("0");
        discountTypeComboBox.setSelectedIndex(0);
        paymentMethodComboBox.setSelectedIndex(0);
        quantitySpinner.setValue(1);

        JButton printReceiptButton = findPrintReceiptButton();
        if (printReceiptButton != null) {
            printReceiptButton.setEnabled(false);
        }



    }



    // Receipt printable class to handle receipt printing
    private class ReceiptPrintable implements Printable {
        private Sale sale;

        public ReceiptPrintable(Sale sale) {
            this.sale = sale;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }



            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            Font regularFont = new Font("Monospaced", Font.PLAIN, 10);
            Font boldFont = new Font("Monospaced", Font.BOLD, 12);
            Font headerFont = new Font("Monospaced", Font.BOLD, 14);

            int y = 20;
            int lineHeight = 15;
            float pageWidth = (float) pageFormat.getImageableWidth();

            if (includeLogoOnReceipt) {
                // Logo code (this is a placeholder, as actual logo printing would require image handling)
                g2d.drawString("[LOGO]", (pageWidth - g2d.getFontMetrics().stringWidth("[LOGO]")) / 2, y - 30);
            }



            // Header
            g2d.setFont(headerFont);
            String header = SHOP_NAME;
            float headerWidth = g2d.getFontMetrics().stringWidth(header);
            g2d.drawString(header, (pageWidth - headerWidth) / 2, y);
            y += lineHeight + 5;

            // Shop info
            g2d.setFont(regularFont);

            String address = SHOP_ADDRESS;
            float addressWidth = g2d.getFontMetrics().stringWidth(address);
            g2d.drawString(address, (pageWidth - addressWidth) / 2, y);
            y += lineHeight;

            String phone = "Tel: " + SHOP_PHONE;
            float phoneWidth = g2d.getFontMetrics().stringWidth(phone);
            g2d.drawString(phone, (pageWidth - phoneWidth) / 2, y);
            y += lineHeight;

            String email = "Email: " + SHOP_EMAIL;
            float emailWidth = g2d.getFontMetrics().stringWidth(email);
            g2d.drawString(email, (pageWidth - emailWidth) / 2, y);
            y += lineHeight;

            String website = SHOP_WEBSITE;
            float websiteWidth = g2d.getFontMetrics().stringWidth(website);
            g2d.drawString(website, (pageWidth - websiteWidth) / 2, y);
            y += lineHeight * 2;

            // Receipt header
            g2d.setFont(boldFont);
            g2d.drawString("RECEIPT", 10, y);
            y += lineHeight;

            // Date and transaction ID
            g2d.setFont(regularFont);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = "Date: " + dateFormat.format(new Date());
            g2d.drawString(dateStr, 10, y);
            y += lineHeight;

            String transactionId = "Transaction ID: " + sale.getId();
            g2d.drawString(transactionId, 10, y);
            y += lineHeight * 2;

            // Items header
            g2d.setFont(boldFont);
            g2d.drawString("Item", 10, y);
            g2d.drawString("Price", 160, y);
            g2d.drawString("Qty", 230, y);
            g2d.drawString("Total", 280, y);
            y += lineHeight;

            // Separator line
            g2d.drawLine(10, y, (int)pageWidth - 10, y);
            y += lineHeight;

            // Items
            g2d.setFont(regularFont);
            for (Sale.SaleItem item : sale.getItems()) {
                g2d.drawString(fitString(item.getProductName(), 20), 10, y);
                g2d.drawString("$" + item.getUnitPrice(), 160, y);
                g2d.drawString(String.valueOf(item.getQuantity()), 230, y);
                g2d.drawString("$" + item.getSubtotal(), 280, y);
                y += lineHeight;
            }

            // Separator line
            y += 5;
            g2d.drawLine(10, y, (int)pageWidth - 10, y);
            y += lineHeight + 5;

            // Total
            g2d.setFont(boldFont);
            g2d.drawString("Total:", 200, y);
            g2d.drawString("$" + sale.getTotalAmount(), 280, y);
            y += lineHeight * 2;

            // Payment method - placeholder
            g2d.setFont(regularFont);
            // In the ReceiptPrintable class, replace the payment method line with:
            g2d.drawString("Payment Method: " + (sale.getPaymentMethod() != null ?
                    sale.getPaymentMethod() : "Cash"), 10, y);
            y += lineHeight * 2;

            if (!customReceiptMessage.isEmpty()) {
                y += lineHeight;
                float customMsgWidth = g2d.getFontMetrics().stringWidth(customReceiptMessage);
                g2d.drawString(customReceiptMessage, (pageWidth - customMsgWidth) / 2, y);
                y += lineHeight;
            }

            // Footer
            if (includeFooterMessage) {
                String footer = RECEIPT_FOOTER;
                float footerWidth = g2d.getFontMetrics().stringWidth(footer);
                g2d.drawString(footer, (pageWidth - footerWidth) / 2, y);
            }

            return PAGE_EXISTS;
        }

        private String fitString(String str, int maxLength) {
            if (str.length() <= maxLength) {
                return str;
            }
            return str.substring(0, maxLength - 3) + "...";
        }
    }


}