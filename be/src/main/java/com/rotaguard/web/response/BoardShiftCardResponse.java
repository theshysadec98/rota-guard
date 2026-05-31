/* Nhom I */
package com.rotaguard.web.response;

import com.rotaguard.domain.enums.SlotKind;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BoardShiftCardResponse {
  Long shiftId;
  Long staffId;
  String staffName;
  String department;
  String role;
  ZonedDateTime startAt;
  ZonedDateTime endAt;
  String shiftType;
  SlotKind slot;
  int revisionCount;
  @Builder.Default List<String> warnings = List.of();
}
