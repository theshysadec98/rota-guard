/* Nhom I */
package com.rotaguard.web.response;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShiftResponse {
  Long id;
  Long staffId;
  ZonedDateTime startAt;
  ZonedDateTime endAt;
  String shiftType;
  int revisionCount;
}
