/* Nhom I */
package com.rotaguard.rule.impl;

import com.rotaguard.domain.entity.FatiguePolicy;
import com.rotaguard.domain.entity.Shift;
import com.rotaguard.domain.entity.Staff;
import com.rotaguard.domain.enums.Severity;
import com.rotaguard.rule.FatigueRule;
import com.rotaguard.rule.model.ViolationDraft;
import com.rotaguard.rule.support.ShiftRuleSupport;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class QuickReturnRule implements FatigueRule {

  @Override
  public String code() {
    return "QUICK_RETURN";
  }

  @Override
  public List<ViolationDraft> evaluate(Staff staff, List<Shift> shifts, FatiguePolicy policy) {
    List<ViolationDraft> violations = new ArrayList<>();
    List<Shift> sorted = shifts.stream().sorted(Comparator.comparing(Shift::getStartAt)).toList();

    for (int i = 0; i < sorted.size() - 1; i++) {
      Shift current = sorted.get(i);
      Shift next = sorted.get(i + 1);

      if (!ShiftRuleSupport.isNightShift(current)) {
        continue;
      }

      double restHours = Duration.between(current.getEndAt(), next.getStartAt()).toMinutes() / 60.0;
      double required = policy.getMinRestHours() / staff.getSensitivityFactor();

      if (restHours < required) {
        String evidence =
            """
            {"prevShiftId":%d,"nextShiftId":%d,"restHours":%.2f,"requiredHours":%.2f}
            """
                .formatted(current.getId(), next.getId(), restHours, required)
                .trim();
        violations.add(
            ViolationDraft.builder()
                .ruleCode(code())
                .severity(Severity.CRITICAL)
                .message(
                    "Rest %.1fh after night shift is below minimum %.1fh"
                        .formatted(restHours, required))
                .evidenceJson(evidence)
                .build());
      }
    }
    return violations;
  }
}
