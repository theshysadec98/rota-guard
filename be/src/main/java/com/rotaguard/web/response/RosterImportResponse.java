/* Nhom I */
package com.rotaguard.web.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RosterImportResponse {
  LocalDate weekStart;
  int staffCreated;
  int staffMatched;
  int shiftsCreated;
  int shiftsUpdated;
  int shiftRows;
  List<ImportLineError> errors;
}
