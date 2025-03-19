package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int id;
    private int userId;
    private BigDecimal totalAmount;
    private LocalDateTime saleDate;
    private List<SaleItem> items;
    private String paymentMethod;
    private double discountAmount = 0.0;
    private String discountType = "None";

    public static class SaleItem {
        private int productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;

        public SaleItem(int productId, String productName, int quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getSubtotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
    }

    public Sale() {
        items = new ArrayList<>();
        totalAmount = BigDecimal.ZERO;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public BigDecimal getTotalAmount() {
        BigDecimal rawTotal = calculateRawTotal();
        BigDecimal discountBD = BigDecimal.valueOf(discountAmount);
        return rawTotal.subtract(discountBD).max(BigDecimal.ZERO);
    }

    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }

    public List<SaleItem> getItems() { return items; }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void addItem(Product product, int quantity) {
        SaleItem item = new SaleItem(
                product.getId(),
                product.getName(),
                quantity,
                product.getPrice()
        );
        items.add(item);
        recalculateTotal();
    }

    private void recalculateTotal() {
        totalAmount = calculateRawTotal().subtract(BigDecimal.valueOf(discountAmount)).max(BigDecimal.ZERO);
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
        recalculateTotal();
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public String getDiscountType() {
        return discountType;
    }

    public BigDecimal calculateRawTotal() {
        BigDecimal rawTotal = BigDecimal.ZERO;
        for (SaleItem item : items) {
            rawTotal = rawTotal.add(item.getSubtotal());
        }
        return rawTotal;
    }
}