package org.example.service;

import org.example.dao.ProductDAO;
import org.example.dao.SaleDAO;
import org.example.model.Product;
import org.example.model.Sale;

import java.time.LocalDateTime;
import java.util.List;

public class SaleService {
    private SaleDAO saleDAO;
    private ProductDAO productDAO;

    public SaleService() {
        saleDAO = new SaleDAO();
        productDAO = new ProductDAO();
    }

    public boolean processSale(Sale sale) {
        // Check product availability
        for (Sale.SaleItem item : sale.getItems()) {
            Product product = productDAO.getProductById(item.getProductId());
            if (product == null || product.getStock() < item.getQuantity()) {
                return false; // Not enough stock
            }
        }

        // Process the sale
        return saleDAO.createSale(sale);
    }

    public Sale createNewSale() {
        Sale sale = new Sale();
        sale.setUserId(AuthService.getCurrentUser().getId());
        sale.setSaleDate(LocalDateTime.now());
        return sale;
    }

    public void addItemToSale(Sale sale, Product product, int quantity) {
        if (product.getStock() >= quantity) {
            sale.addItem(product, quantity);
        }
    }

    public List<Sale> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return saleDAO.getSalesByDateRange(startDate, endDate);
    }
}