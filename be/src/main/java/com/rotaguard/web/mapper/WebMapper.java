/* Nhom I */
package com.rotaguard.web.mapper;

import com.rotaguard.domain.entity.AnalysisRun;
import com.rotaguard.domain.entity.FatiguePolicy;
import com.rotaguard.domain.entity.FatigueScore;
import com.rotaguard.domain.entity.Shift;
import com.rotaguard.domain.entity.Staff;
import com.rotaguard.domain.entity.Violation;
import com.rotaguard.rule.model.ViolationDraft;
import com.rotaguard.service.model.ShiftChangeCommand;
import com.rotaguard.service.model.StaffAnalysisResult;
import com.rotaguard.web.request.CreatePolicyRequest;
import com.rotaguard.web.request.ShiftChangeRequest;
import com.rotaguard.web.response.AnalysisReportResponse;
import com.rotaguard.web.response.AnalysisSummaryResponse;
import com.rotaguard.web.response.PolicyDiffEntryResponse;
import com.rotaguard.web.response.PolicyDiffResponse;
import com.rotaguard.web.response.PolicyResponse;
import com.rotaguard.web.response.RunAnalysisResponse;
import com.rotaguard.web.response.ShiftResponse;
import com.rotaguard.web.response.StaffResponse;
import com.rotaguard.web.response.StaffRiskResponse;
import com.rotaguard.web.response.ViolationResponse;
import com.rotaguard.web.response.WhatIfResponse;
import com.rotaguard.web.response.WhatIfStaffDeltaResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WebMapper {

  public static RunAnalysisResponse toRunResponse(AnalysisRun run) {
    return RunAnalysisResponse.builder()
        .runId(run.getId())
        .weekStart(run.getWeekStart())
        .policyId(run.getPolicyId())
        .runType(run.getRunType())
        .build();
  }

  public static StaffResponse toStaffResponse(Staff staff) {
    return StaffResponse.builder()
        .id(staff.getId())
        .name(staff.getName())
        .role(staff.getRole())
        .department(staff.getDepartment())
        .sensitivityFactor(staff.getSensitivityFactor())
        .build();
  }

  public static ShiftResponse toShiftResponse(Shift shift) {
    return ShiftResponse.builder()
        .id(shift.getId())
        .staffId(shift.getStaffId())
        .startAt(shift.getStartAt())
        .endAt(shift.getEndAt())
        .shiftType(shift.getShiftType())
        .revisionCount(shift.getRevisionCount())
        .build();
  }

  public static PolicyResponse toPolicyResponse(FatiguePolicy policy) {
    return PolicyResponse.builder()
        .id(policy.getId())
        .name(policy.getName())
        .department(policy.getDepartment())
        .minRestHours(policy.getMinRestHours())
        .maxConsecutiveNights(policy.getMaxConsecutiveNights())
        .maxWeeklyHours(policy.getMaxWeeklyHours())
        .churnThreshold(policy.getChurnThreshold())
        .build();
  }

  public static FatiguePolicy toEntity(CreatePolicyRequest request) {
    return FatiguePolicy.builder()
        .name(request.getName())
        .department(request.getDepartment())
        .minRestHours(request.getMinRestHours())
        .maxConsecutiveNights(request.getMaxConsecutiveNights())
        .maxWeeklyHours(request.getMaxWeeklyHours())
        .churnThreshold(request.getChurnThreshold())
        .build();
  }

  public static ShiftChangeCommand toCommand(ShiftChangeRequest request) {
    return ShiftChangeCommand.builder()
        .shiftId(request.getShiftId())
        .newStaffId(request.getNewStaffId())
        .build();
  }

  public static ViolationResponse toViolationResponse(Violation violation) {
    return ViolationResponse.builder()
        .ruleCode(violation.getRuleCode())
        .severity(violation.getSeverity())
        .message(violation.getMessage())
        .evidenceJson(violation.getEvidenceJson())
        .build();
  }

  public static StaffRiskResponse toStaffRiskResponse(
      Staff staff, FatigueScore score, List<Violation> violations) {
    return StaffRiskResponse.builder()
        .staffId(staff.getId())
        .staffName(staff.getName())
        .totalPoints(score.getTotalPoints())
        .riskLevel(score.getRiskLevel())
        .churnIndex(score.getChurnIndex())
        .violations(violations.stream().map(WebMapper::toViolationResponse).toList())
        .build();
  }

  public static AnalysisReportResponse toReportResponse(
      AnalysisRun run, FatiguePolicy policy, List<StaffRiskResponse> staffRisks) {
    int green = 0;
    int yellow = 0;
    int red = 0;
    for (StaffRiskResponse risk : staffRisks) {
      switch (risk.getRiskLevel()) {
        case "GREEN" -> green++;
        case "YELLOW" -> yellow++;
        case "RED" -> red++;
        default -> {}
      }
    }
    return AnalysisReportResponse.builder()
        .runId(run.getId())
        .weekStart(run.getWeekStart())
        .policyId(policy.getId())
        .policyName(policy.getName())
        .runType(run.getRunType())
        .summary(
            AnalysisSummaryResponse.builder()
                .greenCount(green)
                .yellowCount(yellow)
                .redCount(red)
                .build())
        .staffRisks(staffRisks)
        .build();
  }

  public static PolicyDiffResponse toPolicyDiffResponse(
      LocalDate weekStart,
      Long policyIdA,
      Long policyIdB,
      long additionalRedCount,
      List<PolicyDiffEntryResponse> diffs) {
    return PolicyDiffResponse.builder()
        .weekStart(weekStart)
        .policyIdA(policyIdA)
        .policyIdB(policyIdB)
        .additionalRedCount(additionalRedCount)
        .diffs(diffs)
        .build();
  }

  public static PolicyDiffEntryResponse toPolicyDiffEntry(
      Staff staff, StaffAnalysisResult resultA, StaffAnalysisResult resultB) {
    return PolicyDiffEntryResponse.builder()
        .staffId(staff.getId())
        .staffName(staff.getName())
        .riskA(resultA.getScore().getRiskLevel().name())
        .riskB(resultB.getScore().getRiskLevel().name())
        .pointsDelta(resultB.getScore().getTotalPoints() - resultA.getScore().getTotalPoints())
        .violationCountDelta(resultB.getViolations().size() - resultA.getViolations().size())
        .build();
  }

  public static WhatIfStaffDeltaResponse toWhatIfDelta(
      Staff staff, StaffAnalysisResult before, StaffAnalysisResult after, List<String> changes) {
    return WhatIfStaffDeltaResponse.builder()
        .staffId(staff.getId())
        .staffName(staff.getName())
        .riskBefore(before.getScore().getRiskLevel().name())
        .riskAfter(after.getScore().getRiskLevel().name())
        .violationsBefore(before.getViolations().size())
        .violationsAfter(after.getViolations().size())
        .changes(changes)
        .build();
  }

  public static WhatIfResponse toWhatIfResponse(
      LocalDate weekStart, Long policyId, List<WhatIfStaffDeltaResponse> deltas) {
    return WhatIfResponse.builder().weekStart(weekStart).policyId(policyId).deltas(deltas).build();
  }

  public static List<String> diffViolations(
      List<ViolationDraft> before, List<ViolationDraft> after) {
    List<String> changes = new java.util.ArrayList<>();
    for (ViolationDraft b : before) {
      boolean stillPresent =
          after.stream()
              .anyMatch(
                  a ->
                      a.getRuleCode().equals(b.getRuleCode())
                          && a.getMessage().equals(b.getMessage()));
      if (!stillPresent) {
        changes.add("RESOLVED: " + b.getRuleCode() + " — " + b.getMessage());
      }
    }
    for (ViolationDraft a : after) {
      boolean wasPresent =
          before.stream()
              .anyMatch(
                  b ->
                      b.getRuleCode().equals(a.getRuleCode())
                          && b.getMessage().equals(a.getMessage()));
      if (!wasPresent) {
        changes.add("NEW: " + a.getRuleCode() + " — " + a.getMessage());
      }
    }
    return changes;
  }
}
