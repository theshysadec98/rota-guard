/* Nhom I */
package com.rotaguard.web.response;

import com.rotaguard.domain.enums.SlotKind;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WeekBoardResponse {
  LocalDate weekStart;
  LocalDate weekEnd;
  List<SlotKind> slots;
  List<WeekBoardDayResponse> days;
  WeekBoardSummaryResponse summary;
}
