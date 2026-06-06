/* By Ly Minh Hoang */
package com.rotaguard.jdbc;

import com.rotaguard.jdbc.db.SchemaInitializer;
import com.rotaguard.jdbc.db.StaffDao;
import com.rotaguard.jdbc.ui.StaffFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
  private Main() {}

  public static void main(String[] args) {
    SchemaInitializer.initialize();
    configureLookAndFeel();

    SwingUtilities.invokeLater(
        () -> {
          StaffFrame frame = new StaffFrame(new StaffDao());
          frame.setVisible(true);
        });
  }

  private static void configureLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ignored) {
    }
  }
}
