/* Nhom I */
package com.rotaguard.web.response;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StaffImportResponse {
  int created;
  int updated;
  int skipped;
  int total;
  List<ImportLineError> errors;
}
