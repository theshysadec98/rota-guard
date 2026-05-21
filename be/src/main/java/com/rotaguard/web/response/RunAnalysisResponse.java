/* Nhom I */
package com.rotaguard.web.response;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RunAnalysisResponse {
  Long runId;
  LocalDate weekStart;
  Long policyId;
  String runType;
}
