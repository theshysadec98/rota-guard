/* Nhom I */
package com.rotaguard.web.response;

import com.rotaguard.domain.enums.SlotKind;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WeekBoardDayResponse {
  LocalDate date;
  String label;
  Map<SlotKind, List<BoardShiftCardResponse>> cells;
}
