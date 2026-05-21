/* Nhom I */
package com.rotaguard.rule.impl;

import com.rotaguard.domain.entity.FatiguePolicy;
import com.rotaguard.domain.entity.Shift;
import com.rotaguard.domain.entity.Staff;
import com.rotaguard.domain.enums.Severity;
import com.rotaguard.rule.FatigueRule;
import com.rotaguard.rule.model.ViolationDraft;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class WeeklyHoursRule implements FatigueRule {

  @Override
  public String code() {
    return "WEEKLY_HOURS";
  }

  @Override
  public List<ViolationDraft> evaluate(Staff staff, List<Shift> shifts, FatiguePolicy policy) {
    double totalHours =
        shifts.stream()
            .mapToDouble(s -> Duration.between(s.getStartAt(), s.getEndAt()).toMinutes() / 60.0)
            .sum();

    double maxAllowed = policy.getMaxWeeklyHours() / staff.getSensitivityFactor();

    if (totalHours <= maxAllowed) {
      return List.of();
    }

    String evidence =
        """
        {"totalHours":%.2f,"maxWeeklyHours":%.2f,"shiftCount":%d}
        """
            .formatted(totalHours, maxAllowed, shifts.size())
            .trim();

    return List.of(
        ViolationDraft.builder()
            .ruleCode(code())
            .severity(Severity.WARN)
            .message("Weekly hours %.1fh exceeds maximum %.1fh".formatted(totalHours, maxAllowed))
            .evidenceJson(evidence)
            .build());
  }
}
