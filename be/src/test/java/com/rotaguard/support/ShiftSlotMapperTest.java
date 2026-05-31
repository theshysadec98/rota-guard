/* Nhom I */
package com.rotaguard.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.rotaguard.domain.enums.SlotKind;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class ShiftSlotMapperTest {

  @Test
  void mapsMorningAfternoonNight() {
    LocalDate d = LocalDate.of(2025, 6, 9);
    assertEquals(
        SlotKind.MORNING,
        ShiftSlotMapper.fromStartAt(d.atTime(7, 0).atZone(AppTimeZones.APP_ZONE)));
    assertEquals(
        SlotKind.AFTERNOON,
        ShiftSlotMapper.fromStartAt(d.atTime(14, 0).atZone(AppTimeZones.APP_ZONE)));
    assertEquals(
        SlotKind.NIGHT, ShiftSlotMapper.fromStartAt(d.atTime(22, 0).atZone(AppTimeZones.APP_ZONE)));
    assertEquals(
        SlotKind.NIGHT, ShiftSlotMapper.fromStartAt(d.atTime(5, 30).atZone(AppTimeZones.APP_ZONE)));
  }

  @Test
  void boardDateUsesStartInAppZone() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 9, 22, 0, 0, 0, AppTimeZones.APP_ZONE);
    assertEquals(LocalDate.of(2025, 6, 9), ShiftSlotMapper.boardDate(start));
  }
}
