/* Nhom I */
package com.rotaguard.web.response;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorResponse {
  String error;
  Map<String, String> fieldErrors;
  List<ImportLineError> importErrors;
}
