/* Nhom I */
package com.rotaguard.service;

import com.rotaguard.domain.entity.FatiguePolicy;
import com.rotaguard.domain.entity.Shift;
import com.rotaguard.domain.entity.Staff;
import com.rotaguard.rule.FatigueRule;
import com.rotaguard.rule.model.ViolationDraft;
import com.rotaguard.service.model.StaffAnalysisResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RotaAnalyzer {

  private final List<FatigueRule> rules;

  public List<StaffAnalysisResult> analyze(
      List<Staff> staffList, List<Shift> weekShifts, FatiguePolicy policy) {
    Map<Long, List<Shift>> byStaff =
        weekShifts.stream().collect(Collectors.groupingBy(Shift::getStaffId));

    List<StaffAnalysisResult> results = new ArrayList<>();
    for (Staff staff : staffList) {
      List<Shift> shifts = byStaff.getOrDefault(staff.getId(), List.of());
      List<ViolationDraft> violations = new ArrayList<>();
      for (FatigueRule rule : rules) {
        violations.addAll(rule.evaluate(staff, shifts, policy));
      }
      int churnIndex = ScoringService.cappedChurnIndex(shifts);
      var score = ScoringService.score(violations, churnIndex, policy);
      results.add(
          StaffAnalysisResult.builder()
              .staff(staff)
              .score(score)
              .violations(violations)
              .churnIndex(churnIndex)
              .build());
    }
    return results;
  }
}
