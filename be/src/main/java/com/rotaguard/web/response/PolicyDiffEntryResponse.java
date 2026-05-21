/* Nhom I */
package com.rotaguard.web.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PolicyDiffEntryResponse {
  Long staffId;
  String staffName;
  String riskA;
  String riskB;
  int pointsDelta;
  int violationCountDelta;
}
