/* Nhom I */
package com.rotaguard.support;

import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WeekUtils {

  public static void requireMonday(LocalDate weekStart) {
    if (weekStart == null || weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
      throw new IllegalArgumentException("weekStart must be a Monday (ISO week)");
    }
  }
}
