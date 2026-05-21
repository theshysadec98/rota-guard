/* Nhom I */
package com.rotaguard.domain.enums;

import java.util.Locale;
import org.springframework.lang.Nullable;

public enum StaffListSortField {
  NAME,
  ID,
  ROLE,
  DEPARTMENT;

  public static StaffListSortField fromParam(@Nullable String raw) {
    if (raw == null || raw.isBlank()) {
      return NAME;
    }
    return switch (raw.trim().toUpperCase(Locale.ROOT)) {
      case "ID" -> ID;
      case "ROLE" -> ROLE;
      case "DEPARTMENT", "DEPT" -> DEPARTMENT;
      default -> NAME;
    };
  }
}
