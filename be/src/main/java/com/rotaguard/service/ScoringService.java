/* Nhom I */
package com.rotaguard.service;

import com.rotaguard.domain.entity.FatiguePolicy;
import com.rotaguard.domain.entity.Shift;
import com.rotaguard.domain.enums.RiskLevel;
import com.rotaguard.domain.enums.Severity;
import com.rotaguard.rule.model.ViolationDraft;
import com.rotaguard.service.model.ScoreResult;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ScoringService {

  public static int cappedChurnIndex(List<Shift> shifts) {
    int raw = shifts.stream().mapToInt(Shift::getRevisionCount).sum() * 15;
    return Math.min(100, raw);
  }

  public static ScoreResult score(
      List<ViolationDraft> drafts, int churnIndex, FatiguePolicy policy) {
    int violationPoints = drafts.stream().mapToInt(d -> d.getSeverity().getWeight()).sum();
    int totalPoints = violationPoints + churnIndex / 20;
    boolean hasCritical = drafts.stream().anyMatch(d -> d.getSeverity() == Severity.CRITICAL);
    RiskLevel level = toRiskLevel(totalPoints, hasCritical, churnIndex, policy.getChurnThreshold());
    return ScoreResult.builder()
        .totalPoints(totalPoints)
        .riskLevel(level)
        .churnIndex(churnIndex)
        .build();
  }

  private static RiskLevel toRiskLevel(
      int totalPoints, boolean hasCritical, int churnIndex, int churnThreshold) {
    if (hasCritical || totalPoints >= 5) {
      return RiskLevel.RED;
    }
    if (totalPoints >= 1 && churnIndex >= churnThreshold) {
      return RiskLevel.RED;
    }
    if (totalPoints >= 1) {
      return RiskLevel.YELLOW;
    }
    return RiskLevel.GREEN;
  }
}
