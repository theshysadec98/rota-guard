/* Nhom I */
package com.rotaguard.rule.model;

import com.rotaguard.domain.enums.Severity;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ViolationDraft {
  String ruleCode;
  Severity severity;
  String message;
  String evidenceJson;
}
