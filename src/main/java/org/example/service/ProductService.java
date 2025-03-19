package org.example.service;

import org.example.dao.ProductDAO;
import org.example.model.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ProductService {
    private ProductDAO productDAO;

    public ProductService() {
        productDAO = new ProductDAO();
    }

    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    public Product getProductById(int id) {
        return productDAO.getProductById(id);
    }

    public boolean createProduct(String name, BigDecimal price, int stock, LocalDate expiryDate, String category) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        product.setExpiryDate(expiryDate);
        product.setCategory(category);

        return productDAO.createProduct(product);
    }

    public boolean updateProduct(int id, String name, BigDecimal price, int stock, LocalDate expiryDate, String category) {
        Product product = productDAO.getProductById(id);
        if (product == null) {
            return false;
        }

        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        product.setExpiryDate(expiryDate);
        product.setCategory(category);

        return productDAO.updateProduct(product);
    }

    public boolean deleteProduct(int id) {
        return productDAO.deleteProduct(id);
    }

    public List<Product> getExpiringProducts() {
        return productDAO.getExpiringProducts();
    }

    public List<Product> getLowStockProducts() {
        return productDAO.getLowStockProducts();
    }
}