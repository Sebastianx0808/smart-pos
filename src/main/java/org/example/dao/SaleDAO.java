package org.example.dao;

import org.example.model.Sale;
import org.example.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    public boolean createSale(Sale sale) {
        String saleSql = "INSERT INTO sales (user_id, total_amount, sale_date) VALUES (?, ?, ?)";
        String itemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // Insert sale
            try (PreparedStatement stmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, sale.getUserId());
                stmt.setBigDecimal(2, sale.getTotalAmount());

                if (sale.getSaleDate() != null) {
                    stmt.setTimestamp(3, Timestamp.valueOf(sale.getSaleDate()));
                } else {
                    stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                }

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating sale failed, no rows affected.");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        sale.setId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating sale failed, no ID obtained.");
                    }
                }
            }

            // Insert sale items
            try (PreparedStatement stmt = conn.prepareStatement(itemSql)) {
                for (Sale.SaleItem item : sale.getItems()) {
                    stmt.setInt(1, sale.getId());
                    stmt.setInt(2, item.getProductId());
                    stmt.setInt(3, item.getQuantity());
                    stmt.setBigDecimal(4, item.getUnitPrice());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            // Update product stock
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE products SET stock = stock - ? WHERE id = ?")) {
                for (Sale.SaleItem item : sale.getItems()) {
                    stmt.setInt(1, item.getQuantity());
                    stmt.setInt(2, item.getProductId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error rolling back transaction: " + ex.getMessage());
                }
            }
            System.err.println("Error creating sale: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error resetting auto-commit: " + e.getMessage());
                }
            }
        }
    }

    public List<Sale> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE sale_date BETWEEN ? AND ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = new Sale();
                    sale.setId(rs.getInt("id"));
                    sale.setUserId(rs.getInt("user_id"));
                    sale.setTotalAmount(rs.getBigDecimal("total_amount"));
                    sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());

                    // Get sale items
                    loadSaleItems(sale);

                    sales.add(sale);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving sales: " + e.getMessage());
        }

        return sales;
    }

    private void loadSaleItems(Sale sale) {
        String sql = "SELECT si.*, p.name FROM sale_items si " +
                "JOIN products p ON si.product_id = p.id " +
                "WHERE si.sale_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sale.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("product_id");
                    String productName = rs.getString("name");
                    int quantity = rs.getInt("quantity");
                    java.math.BigDecimal unitPrice = rs.getBigDecimal("unit_price");

                    Sale.SaleItem item = new Sale.SaleItem(productId, productName, quantity, unitPrice);
                    sale.getItems().add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving sale items: " + e.getMessage());
        }
    }
}