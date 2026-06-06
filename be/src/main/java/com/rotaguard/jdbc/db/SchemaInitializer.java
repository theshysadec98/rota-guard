/* By Ly Minh Hoang */
package com.rotaguard.jdbc.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaInitializer {
  private SchemaInitializer() {}

  public static void initialize() {
    try (Connection connection = DatabaseConfig.getConnection()) {
      createTable(connection);
      seedDataIfEmpty(connection);
    } catch (SQLException exception) {
      throw new IllegalStateException("Khong the khoi tao bang staff", exception);
    }
  }

  private static void createTable(Connection connection) throws SQLException {
    String sql =
        """
        CREATE TABLE IF NOT EXISTS staff (
            ma_nv VARCHAR(20) PRIMARY KEY,
            ho_ten VARCHAR(255) NOT NULL,
            khoa VARCHAR(100) NOT NULL,
            he_so DOUBLE PRECISION NOT NULL DEFAULT 1.0,
            vai_tro VARCHAR(20) NOT NULL DEFAULT 'NURSE',
            sdt VARCHAR(15),
            email VARCHAR(255),
            nam_kn INT NOT NULL DEFAULT 0
        )
        """;
    try (Statement statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }

  private static void seedDataIfEmpty(Connection connection) throws SQLException {
    try (PreparedStatement countStatement =
            connection.prepareStatement("SELECT COUNT(*) FROM staff");
        ResultSet resultSet = countStatement.executeQuery()) {
      resultSet.next();
      if (resultSet.getInt(1) > 0) {
        return;
      }
    }

    String insertSql =
        """
        INSERT INTO staff (ma_nv, ho_ten, khoa, he_so, vai_tro, sdt, email, nam_kn)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
    try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
      insertSeed(
          statement,
          "NV001",
          "Nguyen Thi Lan",
          "ICU",
          1.0,
          "NURSE",
          "0901000001",
          "lan.nguyen@bv.vn",
          5);
      insertSeed(
          statement,
          "NV002",
          "Tran Van Minh",
          "ICU",
          1.2,
          "DOCTOR",
          "0901000002",
          "minh.tran@bv.vn",
          12);
      insertSeed(statement, "NV003", "Le Hoang Nam", "ER", 1.0, "NURSE", "0901000003", null, 2);
    }
  }

  private static void insertSeed(
      PreparedStatement statement,
      String maNv,
      String hoTen,
      String khoa,
      double heSo,
      String vaiTro,
      String sdt,
      String email,
      int namKn)
      throws SQLException {
    statement.setString(1, maNv);
    statement.setString(2, hoTen);
    statement.setString(3, khoa);
    statement.setDouble(4, heSo);
    statement.setString(5, vaiTro);
    statement.setString(6, sdt);
    statement.setString(7, email);
    statement.setInt(8, namKn);
    statement.executeUpdate();
  }
}
