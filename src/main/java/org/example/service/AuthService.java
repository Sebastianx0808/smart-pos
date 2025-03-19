package org.example.service;

import org.example.dao.UserDAO;
import org.example.model.User;

public class AuthService {
    private UserDAO userDAO;
    private static User currentUser;

    public AuthService() {
        userDAO = new UserDAO();
    }

    public boolean login(String username, String password) {
        User user = userDAO.authenticate(username, password);
        if (user != null) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }

    public boolean isCashier() {
        return currentUser != null && "CASHIER".equals(currentUser.getRole());
    }
}