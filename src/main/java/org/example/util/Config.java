package org.example.util;

public class Config {
    public static final String DB_URL = "jdbc:mysql://localhost:3306/pos_db";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "sjmtechcorp"; // Change this to your actual MySQL password

    // Application configuration
    public static final String APP_NAME = "Retail POS System";
    public static final String APP_VERSION = "1.0";

    // Feature flags
    public static final boolean ENABLE_EXPIRY_TRACKING = true;
}
