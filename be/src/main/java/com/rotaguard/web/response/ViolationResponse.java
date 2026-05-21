/* Nhom I */
package com.rotaguard.web.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ViolationResponse {
  String ruleCode;
  String severity;
  String message;
  String evidenceJson;
}
