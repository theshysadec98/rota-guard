/* Nhom I */
package com.rotaguard.web.response;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShiftImportResponse {
  int created;
  int updated;
  int total;
  List<ImportLineError> errors;
}
