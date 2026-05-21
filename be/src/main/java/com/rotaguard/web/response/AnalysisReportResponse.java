/* Nhom I */
package com.rotaguard.web.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AnalysisReportResponse {
  Long runId;
  LocalDate weekStart;
  Long policyId;
  String policyName;
  String runType;
  AnalysisSummaryResponse summary;
  List<StaffRiskResponse> staffRisks;
}
