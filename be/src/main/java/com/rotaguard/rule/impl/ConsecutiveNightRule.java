/* Nhom I */
package com.rotaguard.rule.impl;

import com.rotaguard.domain.entity.FatiguePolicy;
import com.rotaguard.domain.entity.Shift;
import com.rotaguard.domain.entity.Staff;
import com.rotaguard.domain.enums.Severity;
import com.rotaguard.rule.FatigueRule;
import com.rotaguard.rule.model.ViolationDraft;
import com.rotaguard.rule.support.ShiftRuleSupport;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ConsecutiveNightRule implements FatigueRule {

  @Override
  public String code() {
    return "CONSECUTIVE_NIGHT";
  }

  @Override
  public List<ViolationDraft> evaluate(Staff staff, List<Shift> shifts, FatiguePolicy policy) {
    List<ViolationDraft> violations = new ArrayList<>();
    List<Shift> sorted = shifts.stream().sorted(Comparator.comparing(Shift::getStartAt)).toList();

    int streak = 0;
    List<Long> streakIds = new ArrayList<>();

    for (Shift shift : sorted) {
      if (ShiftRuleSupport.isNightShift(shift)) {
        streak++;
        streakIds.add(shift.getId());
      } else {
        streak = 0;
        streakIds.clear();
      }

      if (streak > policy.getMaxConsecutiveNights()) {
        String evidence =
            """
            {"shiftIds":%s,"consecutiveNights":%d,"maxAllowed":%d}
            """
                .formatted(streakIds, streak, policy.getMaxConsecutiveNights())
                .trim();
        violations.add(
            ViolationDraft.builder()
                .ruleCode(code())
                .severity(Severity.WARN)
                .message(
                    "%d consecutive night shifts exceeds max %d"
                        .formatted(streak, policy.getMaxConsecutiveNights()))
                .evidenceJson(evidence)
                .build());
      }
    }
    return violations;
  }
}
