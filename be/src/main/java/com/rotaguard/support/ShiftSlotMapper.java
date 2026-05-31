/* Nhom I */
package com.rotaguard.support;

import com.rotaguard.domain.enums.SlotKind;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ShiftSlotMapper {

  public static SlotKind fromStartAt(ZonedDateTime startAt) {
    int hour = startAt.withZoneSameInstant(AppTimeZones.APP_ZONE).getHour();
    if (hour >= 6 && hour < 14) {
      return SlotKind.MORNING;
    }
    if (hour >= 14 && hour < 22) {
      return SlotKind.AFTERNOON;
    }
    return SlotKind.NIGHT;
  }

  public static LocalDate boardDate(ZonedDateTime startAt) {
    return startAt.withZoneSameInstant(AppTimeZones.APP_ZONE).toLocalDate();
  }
}
