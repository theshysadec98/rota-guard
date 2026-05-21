/* Nhom I */
package com.rotaguard.support;

public final class TextSearchUtils {

  private TextSearchUtils() {}

  public static String toLikePattern(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    return "%" + escapeLike(raw.trim()) + "%";
  }

  public static String escapeLike(String value) {
    return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
