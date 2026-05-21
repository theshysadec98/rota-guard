/* Nhom I */
package com.rotaguard.rule.support;

import com.rotaguard.domain.entity.Shift;
import com.rotaguard.domain.enums.ShiftType;
import com.rotaguard.support.AppTimeZones;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ShiftRuleSupport {

  public static boolean isNightShift(Shift shift) {
    if (shift.typeEnum() == ShiftType.NIGHT) {
      return true;
    }
    int hour = shift.getEndAt().withZoneSameInstant(AppTimeZones.APP_ZONE).getHour();
    return hour >= 22 || hour < 6;
  }
}
