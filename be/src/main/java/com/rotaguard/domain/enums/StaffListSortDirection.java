/* Nhom I */
package com.rotaguard.domain.enums;

import org.springframework.lang.Nullable;

public enum StaffListSortDirection {
  ASC,
  DESC;

  public static StaffListSortDirection fromParam(@Nullable String raw) {
    if (raw == null || raw.isBlank()) {
      return ASC;
    }
    return "DESC".equalsIgnoreCase(raw.trim()) ? DESC : ASC;
  }
}
