/* Nhom I */
package com.rotaguard.web.response;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StaffRiskResponse {
  Long staffId;
  String staffName;
  int totalPoints;
  String riskLevel;
  int churnIndex;
  List<ViolationResponse> violations;
}
