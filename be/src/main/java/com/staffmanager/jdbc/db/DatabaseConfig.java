/* By Ly Minh Hoang */
package com.staffmanager.jdbc.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfig {
  private static final Properties PROPERTIES = loadProperties();

  private DatabaseConfig() {}

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(getUrl(), getUsername(), getPassword());
  }

  private static Properties loadProperties() {
    Properties properties = new Properties();
    try (InputStream inputStream =
        DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
      if (inputStream != null) {
        properties.load(inputStream);
      }
    } catch (IOException exception) {
      throw new IllegalStateException("Không thể đọc file cấu hình db.properties.", exception);
    }
    return properties;
  }

  private static String getUrl() {
    return readValue("STAFF_MANAGER_DB_URL", "db.url");
  }

  private static String getUsername() {
    return readValue("STAFF_MANAGER_DB_USERNAME", "db.username");
  }

  private static String getPassword() {
    return readValue("STAFF_MANAGER_DB_PASSWORD", "db.password");
  }

  private static String readValue(String envKey, String propertyKey) {
    String envValue = System.getenv(envKey);
    if (envValue != null && !envValue.isBlank()) {
      return envValue;
    }
    String propertyValue = PROPERTIES.getProperty(propertyKey);
    if (propertyValue == null || propertyValue.isBlank()) {
      throw new IllegalStateException("Thiếu cấu hình " + propertyKey + " trong db.properties.");
    }
    return propertyValue.trim();
  }
}
