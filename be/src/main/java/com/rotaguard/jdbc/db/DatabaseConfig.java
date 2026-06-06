/* By Ly Minh Hoang */
package com.rotaguard.jdbc.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfig {
  private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/rotaguard";
  private static final String DEFAULT_USERNAME = "rotaguard";
  private static final String DEFAULT_PASSWORD = "rotaguard";
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
      throw new IllegalStateException("Khong the doc db.properties", exception);
    }
    return properties;
  }

  private static String getUrl() {
    return readValue("ROTAGUARD_DB_URL", "db.url", DEFAULT_URL);
  }

  private static String getUsername() {
    return readValue("ROTAGUARD_DB_USERNAME", "db.username", DEFAULT_USERNAME);
  }

  private static String getPassword() {
    return readValue("ROTAGUARD_DB_PASSWORD", "db.password", DEFAULT_PASSWORD);
  }

  private static String readValue(String envKey, String propertyKey, String fallback) {
    String envValue = System.getenv(envKey);
    if (envValue != null && !envValue.isBlank()) {
      return envValue;
    }
    return PROPERTIES.getProperty(propertyKey, fallback).trim();
  }
}
