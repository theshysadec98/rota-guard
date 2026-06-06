/* By Ly Minh Hoang */
package com.staffmanager.jdbc.ui;

import com.staffmanager.jdbc.db.StaffDao;
import com.staffmanager.jdbc.model.Staff;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class StaffFrame extends JFrame {
  private static final String[] ROLE_OPTIONS = {"DOCTOR", "NURSE"};

  private final StaffDao staffDao;
  private final JTextField maNvField = new JTextField(14);
  private final JTextField hoTenField = new JTextField(18);
  private final JTextField khoaField = new JTextField(14);
  private final JTextField heSoField = new JTextField(8);
  private final JComboBox<String> vaiTroComboBox = new JComboBox<>(ROLE_OPTIONS);
  private final JTextField sdtField = new JTextField(14);
  private final JTextField emailField = new JTextField(18);
  private final JTextField namKnField = new JTextField(8);
  private final DefaultTableModel tableModel =
      new DefaultTableModel(
          new String[] {"Mã NV", "Họ tên", "Khoa", "Hệ số", "Vai trò", "SĐT", "Email", "Năm KN"},
          0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable staffTable = new JTable(tableModel);

  public StaffFrame(StaffDao staffDao) {
    this.staffDao = staffDao;
    setTitle("Staff Manager");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout(12, 12));
    setMinimumSize(new Dimension(1100, 620));
    setLocationRelativeTo(null);

    add(buildFormPanel(), BorderLayout.NORTH);
    add(buildTablePanel(), BorderLayout.CENTER);
    add(buildFooterPanel(), BorderLayout.SOUTH);

    registerActions();
    resetForm();
    loadTableData();
    pack();
  }

  private JPanel buildFormPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(6, 6, 6, 6);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    addField(panel, gbc, 0, "Mã NV", maNvField);
    addField(panel, gbc, 1, "Họ tên", hoTenField);
    addField(panel, gbc, 2, "Khoa", khoaField);
    addField(panel, gbc, 3, "Hệ số", heSoField);
    addField(panel, gbc, 4, "Vai trò", vaiTroComboBox);
    addField(panel, gbc, 5, "Năm KN", namKnField);
    addField(panel, gbc, 6, "SĐT", sdtField);
    addField(panel, gbc, 7, "Email", emailField);

    JPanel buttonPanel = new JPanel();
    JButton hienThiButton = new JButton("Hiển thị");
    hienThiButton.addActionListener(event -> loadTableData());

    JButton themButton = new JButton("Thêm");
    themButton.addActionListener(event -> handleInsert());

    JButton capNhatButton = new JButton("Cập nhật");
    capNhatButton.addActionListener(event -> handleUpdate());

    JButton xoaButton = new JButton("Xóa");
    xoaButton.addActionListener(event -> handleDelete());

    JButton resetButton = new JButton("Reset");
    resetButton.addActionListener(event -> resetForm());

    buttonPanel.add(hienThiButton);
    buttonPanel.add(themButton);
    buttonPanel.add(capNhatButton);
    buttonPanel.add(xoaButton);
    buttonPanel.add(resetButton);

    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.gridwidth = 6;
    panel.add(buttonPanel, gbc);
    return panel;
  }

  private JScrollPane buildTablePanel() {
    staffTable.setRowHeight(26);
    staffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    staffTable.getTableHeader().setReorderingAllowed(false);
    return new JScrollPane(staffTable);
  }

  private JPanel buildFooterPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(0, 12, 12, 12));
    JLabel ownerLabel = new JLabel("By Ly Minh Hoang", SwingConstants.RIGHT);
    panel.add(ownerLabel, BorderLayout.EAST);
    return panel;
  }

  private void addField(
      JPanel panel, GridBagConstraints gbc, int index, String label, Component field) {
    int row = index / 3;
    int column = (index % 3) * 2;
    gbc.gridx = column;
    gbc.gridy = row;
    gbc.weightx = 0;
    panel.add(new JLabel(label + ":", SwingConstants.RIGHT), gbc);

    gbc.gridx = column + 1;
    gbc.weightx = 1;
    panel.add(field, gbc);
  }

  private void registerActions() {
    staffTable
        .getSelectionModel()
        .addListSelectionListener(
            event -> {
              if (!event.getValueIsAdjusting()) {
                fillFormFromSelectedRow();
              }
            });
  }

  private void loadTableData() {
    try {
      List<Staff> staffs = staffDao.findAll();
      tableModel.setRowCount(0);
      for (Staff staff : staffs) {
        tableModel.addRow(
            new Object[] {
              staff.getMaNv(),
              staff.getHoTen(),
              staff.getKhoa(),
              staff.getHeSo(),
              staff.getVaiTro(),
              defaultString(staff.getSdt()),
              defaultString(staff.getEmail()),
              staff.getNamKn()
            });
      }
    } catch (RuntimeException exception) {
      showError(exception.getMessage());
    }
  }

  private void handleInsert() {
    try {
      Staff staff = readForm();
      if (staffDao.exists(staff.getMaNv())) {
        showError("Mã nhân viên đã tồn tại.");
        return;
      }
      staffDao.insert(staff);
      loadTableData();
      selectRowByMaNv(staff.getMaNv());
      showInfo("Thêm nhân sự thành công.");
    } catch (IllegalArgumentException exception) {
      showError(exception.getMessage());
    } catch (RuntimeException exception) {
      showError(exception.getMessage());
    }
  }

  private void handleUpdate() {
    try {
      Staff staff = readForm();
      if (!staffDao.exists(staff.getMaNv())) {
        showError("Không tìm thấy mã nhân viên để cập nhật.");
        return;
      }
      staffDao.update(staff);
      loadTableData();
      selectRowByMaNv(staff.getMaNv());
      showInfo("Cập nhật nhân sự thành công.");
    } catch (IllegalArgumentException exception) {
      showError(exception.getMessage());
    } catch (RuntimeException exception) {
      showError(exception.getMessage());
    }
  }

  private void handleDelete() {
    String maNv = maNvField.getText().trim();
    if (maNv.isEmpty()) {
      showError("Vui lòng chọn nhân sự cần xóa.");
      return;
    }

    int confirm =
        JOptionPane.showConfirmDialog(
            this, "Xóa nhân sự " + maNv + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) {
      return;
    }

    try {
      staffDao.delete(maNv);
      loadTableData();
      resetForm();
      showInfo("Xóa nhân sự thành công.");
    } catch (RuntimeException exception) {
      showError(exception.getMessage());
    }
  }

  private Staff readForm() {
    String maNv = requireText(maNvField.getText(), "Mã NV");
    String hoTen = requireText(hoTenField.getText(), "Họ tên");
    String khoa = requireText(khoaField.getText(), "Khoa");
    double heSo = parseNonNegativeDouble(heSoField.getText(), "Hệ số");
    String vaiTro = (String) vaiTroComboBox.getSelectedItem();
    int namKn = parseNonNegativeInt(namKnField.getText(), "Năm KN");
    String sdt = normalizeOptionalText(sdtField.getText());
    String email = normalizeOptionalText(emailField.getText());

    if (email != null && !email.contains("@")) {
      throw new IllegalArgumentException("Email phải chứa ký tự @.");
    }
    if (!"DOCTOR".equals(vaiTro) && !"NURSE".equals(vaiTro)) {
      throw new IllegalArgumentException("Vai trò không hợp lệ.");
    }

    return new Staff(maNv, hoTen, khoa, heSo, vaiTro, sdt, email, namKn);
  }

  private void fillFormFromSelectedRow() {
    int selectedRow = staffTable.getSelectedRow();
    if (selectedRow < 0) {
      return;
    }

    maNvField.setText(stringValue(selectedRow, 0));
    maNvField.setEditable(false);
    hoTenField.setText(stringValue(selectedRow, 1));
    khoaField.setText(stringValue(selectedRow, 2));
    heSoField.setText(stringValue(selectedRow, 3));
    vaiTroComboBox.setSelectedItem(stringValue(selectedRow, 4));
    sdtField.setText(stringValue(selectedRow, 5));
    emailField.setText(stringValue(selectedRow, 6));
    namKnField.setText(stringValue(selectedRow, 7));
  }

  private void resetForm() {
    maNvField.setText("");
    maNvField.setEditable(true);
    hoTenField.setText("");
    khoaField.setText("");
    heSoField.setText("1.0");
    vaiTroComboBox.setSelectedItem("NURSE");
    sdtField.setText("");
    emailField.setText("");
    namKnField.setText("0");
    staffTable.clearSelection();
    maNvField.requestFocusInWindow();
  }

  private void selectRowByMaNv(String maNv) {
    for (int row = 0; row < tableModel.getRowCount(); row++) {
      if (maNv.equals(tableModel.getValueAt(row, 0))) {
        staffTable.setRowSelectionInterval(row, row);
        staffTable.scrollRectToVisible(staffTable.getCellRect(row, 0, true));
        return;
      }
    }
  }

  private String requireText(String value, String fieldName) {
    String normalized = normalizeOptionalText(value);
    if (normalized == null) {
      throw new IllegalArgumentException(fieldName + " không được để trống.");
    }
    return normalized;
  }

  private double parseNonNegativeDouble(String value, String fieldName) {
    try {
      double parsed = Double.parseDouble(requireText(value, fieldName));
      if (parsed < 0) {
        throw new IllegalArgumentException(fieldName + " phải lớn hơn hoặc bằng 0.");
      }
      return parsed;
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException(fieldName + " phải là số thực hợp lệ.");
    }
  }

  private int parseNonNegativeInt(String value, String fieldName) {
    try {
      int parsed = Integer.parseInt(requireText(value, fieldName));
      if (parsed < 0) {
        throw new IllegalArgumentException(fieldName + " phải lớn hơn hoặc bằng 0.");
      }
      return parsed;
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException(fieldName + " phải là số nguyên hợp lệ.");
    }
  }

  private String normalizeOptionalText(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  private String stringValue(int row, int column) {
    Object value = tableModel.getValueAt(row, column);
    return value == null ? "" : value.toString();
  }

  private String defaultString(String value) {
    return value == null ? "" : value;
  }

  private void showInfo(String message) {
    JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
  }

  private void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
  }
}
