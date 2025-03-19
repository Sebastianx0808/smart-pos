package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Product {
    private int id;
    private String name;
    private BigDecimal price;
    private int stock;
    private LocalDate expiryDate;
    private String category;
    private String description; // Added description field

    public Product() {}

    public Product(int id, String name, BigDecimal price, int stock, LocalDate expiryDate, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.expiryDate = expiryDate;
        this.category = category;
    }

    // Extended constructor with description
    public Product(int id, String name, BigDecimal price, int stock, LocalDate expiryDate, String category, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.expiryDate = expiryDate;
        this.category = category;
        this.description = description;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() {
        // Return the description if available, otherwise generate a basic description from existing data
        if (description != null && !description.isEmpty()) {
            return description;
        } else {
            StringBuilder generatedDescription = new StringBuilder();
            generatedDescription.append(name);

            if (category != null && !category.isEmpty()) {
                generatedDescription.append(" - ").append(category);
            }

            if (isLowStock()) {
                generatedDescription.append(" (Low Stock)");
            }

            if (isExpiring()) {
                generatedDescription.append(" (Expiring Soon)");
            }

            return generatedDescription.toString();
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isExpiring() {
        if (expiryDate == null) return false;
        return LocalDate.now().plusDays(30).isAfter(expiryDate);
    }

    public boolean isLowStock() {
        return stock < 10; // Threshold for low stock
    }

    @Override
    public String toString() {
        return name + " ($" + price + ")";
    }
}