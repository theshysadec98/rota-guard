/* Nhom I */
package com.rotaguard.service.model;

import com.rotaguard.domain.enums.RiskLevel;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ScoreResult {
  int totalPoints;
  RiskLevel riskLevel;
  int churnIndex;
}
