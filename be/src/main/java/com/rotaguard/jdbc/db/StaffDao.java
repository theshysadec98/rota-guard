/* By Ly Minh Hoang */
package com.rotaguard.jdbc.db;

import com.rotaguard.jdbc.model.Staff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StaffDao {
  public List<Staff> findAll() {
    String sql =
        """
        SELECT ma_nv, ho_ten, khoa, he_so, vai_tro, sdt, email, nam_kn
        FROM staff
        ORDER BY ma_nv
        """;
    List<Staff> staffs = new ArrayList<>();
    try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {
      while (resultSet.next()) {
        staffs.add(mapRow(resultSet));
      }
      return staffs;
    } catch (SQLException exception) {
      throw new IllegalStateException("Khong the tai danh sach nhan su", exception);
    }
  }

  public void insert(Staff staff) {
    String sql =
        """
        INSERT INTO staff (ma_nv, ho_ten, khoa, he_so, vai_tro, sdt, email, nam_kn)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
    try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      bindInsert(statement, staff);
      statement.executeUpdate();
    } catch (SQLException exception) {
      throw new IllegalStateException("Khong the them nhan su", exception);
    }
  }

  public void update(Staff staff) {
    String sql =
        """
        UPDATE staff
        SET ho_ten = ?, khoa = ?, he_so = ?, vai_tro = ?, sdt = ?, email = ?, nam_kn = ?
        WHERE ma_nv = ?
        """;
    try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, staff.getHoTen());
      statement.setString(2, staff.getKhoa());
      statement.setDouble(3, staff.getHeSo());
      statement.setString(4, staff.getVaiTro());
      statement.setString(5, emptyToNull(staff.getSdt()));
      statement.setString(6, emptyToNull(staff.getEmail()));
      statement.setInt(7, staff.getNamKn());
      statement.setString(8, staff.getMaNv());
      statement.executeUpdate();
    } catch (SQLException exception) {
      throw new IllegalStateException("Khong the cap nhat nhan su", exception);
    }
  }

  public void delete(String maNv) {
    String sql = "DELETE FROM staff WHERE ma_nv = ?";
    try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, maNv);
      statement.executeUpdate();
    } catch (SQLException exception) {
      throw new IllegalStateException("Khong the xoa nhan su", exception);
    }
  }

  public boolean exists(String maNv) {
    String sql = "SELECT 1 FROM staff WHERE ma_nv = ?";
    try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, maNv);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next();
      }
    } catch (SQLException exception) {
      throw new IllegalStateException("Khong the kiem tra ma nhan vien", exception);
    }
  }

  private void bindInsert(PreparedStatement statement, Staff staff) throws SQLException {
    statement.setString(1, staff.getMaNv());
    statement.setString(2, staff.getHoTen());
    statement.setString(3, staff.getKhoa());
    statement.setDouble(4, staff.getHeSo());
    statement.setString(5, staff.getVaiTro());
    statement.setString(6, emptyToNull(staff.getSdt()));
    statement.setString(7, emptyToNull(staff.getEmail()));
    statement.setInt(8, staff.getNamKn());
  }

  private Staff mapRow(ResultSet resultSet) throws SQLException {
    Staff staff = new Staff();
    staff.setMaNv(resultSet.getString("ma_nv"));
    staff.setHoTen(resultSet.getString("ho_ten"));
    staff.setKhoa(resultSet.getString("khoa"));
    staff.setHeSo(resultSet.getDouble("he_so"));
    staff.setVaiTro(resultSet.getString("vai_tro"));
    staff.setSdt(resultSet.getString("sdt"));
    staff.setEmail(resultSet.getString("email"));
    staff.setNamKn(resultSet.getInt("nam_kn"));
    return staff;
  }

  private String emptyToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
