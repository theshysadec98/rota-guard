/* Nhom I */
package com.rotaguard.support;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AppTimeZones {

  public static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  public static ZonedDateTime now() {
    return ZonedDateTime.now(APP_ZONE);
  }

  public static ZonedDateTime startOfWeek(LocalDate weekStart) {
    return weekStart.atStartOfDay(APP_ZONE);
  }

  public static ZonedDateTime endOfWeekExclusive(LocalDate weekStart) {
    return weekStart.plusDays(7).atStartOfDay(APP_ZONE);
  }
}
