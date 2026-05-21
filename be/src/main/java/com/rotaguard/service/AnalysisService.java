/* Nhom I */
package com.rotaguard.service;

import com.rotaguard.domain.entity.AnalysisRun;
import com.rotaguard.domain.entity.FatiguePolicy;
import com.rotaguard.domain.entity.FatigueScore;
import com.rotaguard.domain.entity.Shift;
import com.rotaguard.domain.entity.Staff;
import com.rotaguard.domain.entity.Violation;
import com.rotaguard.domain.enums.RunType;
import com.rotaguard.repository.AnalysisRunRepository;
import com.rotaguard.repository.FatiguePolicyRepository;
import com.rotaguard.repository.FatigueScoreRepository;
import com.rotaguard.repository.ShiftRepository;
import com.rotaguard.repository.StaffRepository;
import com.rotaguard.repository.ViolationRepository;
import com.rotaguard.rule.model.ViolationDraft;
import com.rotaguard.service.model.ShiftChangeCommand;
import com.rotaguard.service.model.StaffAnalysisResult;
import com.rotaguard.support.AppTimeZones;
import com.rotaguard.web.mapper.WebMapper;
import com.rotaguard.web.response.AnalysisReportResponse;
import com.rotaguard.web.response.PolicyDiffEntryResponse;
import com.rotaguard.web.response.PolicyDiffResponse;
import com.rotaguard.web.response.StaffRiskResponse;
import com.rotaguard.web.response.WhatIfResponse;
import com.rotaguard.web.response.WhatIfStaffDeltaResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisService {

  private final StaffRepository staffRepository;
  private final ShiftRepository shiftRepository;
  private final FatiguePolicyRepository policyRepository;
  private final AnalysisRunRepository runRepository;
  private final FatigueScoreRepository scoreRepository;
  private final ViolationRepository violationRepository;
  private final RotaAnalyzer rotaAnalyzer;

  @Transactional
  public AnalysisRun runAnalysis(LocalDate weekStart, Long policyId) {
    FatiguePolicy policy = loadPolicy(policyId);
    List<Staff> staff = staffRepository.findAll();
    List<Shift> shifts = loadWeekShifts(weekStart);

    AnalysisRun run =
        runRepository.save(
            AnalysisRun.builder()
                .weekStart(weekStart)
                .policyId(policyId)
                .runType(RunType.FULL.name())
                .build());

    persistResults(run, staff, shifts, policy);
    return run;
  }

  public AnalysisReportResponse getReport(Long runId) {
    AnalysisRun run =
        runRepository.findById(runId).orElseThrow(() -> notFound("AnalysisRun", runId));
    FatiguePolicy policy = loadPolicy(run.getPolicyId());
    List<FatigueScore> scores = scoreRepository.findByRunId(runId);
    Map<Long, Staff> staffMap =
        staffRepository.findAll().stream().collect(Collectors.toMap(Staff::getId, s -> s));
    Map<Long, List<Violation>> violationsByScore = loadViolations(scores);

    List<StaffRiskResponse> staffRisks = new ArrayList<>();
    for (FatigueScore score : scores) {
      Staff staff = staffMap.get(score.getStaffId());
      staffRisks.add(
          WebMapper.toStaffRiskResponse(
              staff, score, violationsByScore.getOrDefault(score.getId(), List.of())));
    }

    return WebMapper.toReportResponse(run, policy, staffRisks);
  }

  public StaffRiskResponse getStaffRisk(Long runId, Long staffId) {
    FatigueScore score =
        scoreRepository
            .findByRunIdAndStaffId(runId, staffId)
            .orElseThrow(() -> notFound("FatigueScore for staff", staffId));
    Staff staff = staffRepository.findById(staffId).orElseThrow(() -> notFound("Staff", staffId));
    List<Violation> violations = violationRepository.findByScoreId(score.getId());
    return WebMapper.toStaffRiskResponse(staff, score, violations);
  }

  public PolicyDiffResponse policyDiff(LocalDate weekStart, Long policyIdA, Long policyIdB) {
    FatiguePolicy policyA = loadPolicy(policyIdA);
    FatiguePolicy policyB = loadPolicy(policyIdB);
    List<Staff> staff = staffRepository.findAll();
    List<Shift> shifts = loadWeekShifts(weekStart);

    Map<Long, StaffAnalysisResult> resultA = analyzeMap(staff, shifts, policyA);
    Map<Long, StaffAnalysisResult> resultB = analyzeMap(staff, shifts, policyB);

    List<PolicyDiffEntryResponse> diffs = new ArrayList<>();
    for (Staff s : staff) {
      diffs.add(WebMapper.toPolicyDiffEntry(s, resultA.get(s.getId()), resultB.get(s.getId())));
    }

    long redIncrease =
        diffs.stream()
            .filter(d -> !"RED".equals(d.getRiskA()) && "RED".equals(d.getRiskB()))
            .count();

    return WebMapper.toPolicyDiffResponse(weekStart, policyIdA, policyIdB, redIncrease, diffs);
  }

  public Map<Long, StaffAnalysisResult> analyzeWeek(LocalDate weekStart, Long policyId) {
    FatiguePolicy policy = loadPolicy(policyId);
    return analyzeMap(staffRepository.findAll(), loadWeekShifts(weekStart), policy);
  }

  public Map<Long, StaffAnalysisResult> analyzeWeekWithChanges(
      LocalDate weekStart, Long policyId, List<ShiftChangeCommand> changes) {
    FatiguePolicy policy = loadPolicy(policyId);
    List<Staff> staff = staffRepository.findAll();
    List<Shift> modified = applyChanges(loadWeekShifts(weekStart), changes);
    return analyzeMap(staff, modified, policy);
  }

  public WhatIfResponse whatIf(
      LocalDate weekStart, Long policyId, List<ShiftChangeCommand> changes) {
    FatiguePolicy policy = loadPolicy(policyId);
    List<Staff> staff = staffRepository.findAll();
    List<Shift> baseShifts = loadWeekShifts(weekStart);

    Map<Long, StaffAnalysisResult> before = analyzeMap(staff, baseShifts, policy);
    List<Shift> modified = applyChanges(baseShifts, changes);
    Map<Long, StaffAnalysisResult> after = analyzeMap(staff, modified, policy);

    List<WhatIfStaffDeltaResponse> deltas = new ArrayList<>();
    for (Staff s : staff) {
      StaffAnalysisResult b = before.get(s.getId());
      StaffAnalysisResult a = after.get(s.getId());
      deltas.add(
          WebMapper.toWhatIfDelta(
              s, b, a, WebMapper.diffViolations(b.getViolations(), a.getViolations())));
    }
    return WebMapper.toWhatIfResponse(weekStart, policyId, deltas);
  }

  private Map<Long, StaffAnalysisResult> analyzeMap(
      List<Staff> staff, List<Shift> shifts, FatiguePolicy policy) {
    return rotaAnalyzer.analyze(staff, shifts, policy).stream()
        .collect(Collectors.toMap(r -> r.getStaff().getId(), r -> r));
  }

  private void persistResults(
      AnalysisRun run, List<Staff> staff, List<Shift> shifts, FatiguePolicy policy) {
    for (StaffAnalysisResult result : rotaAnalyzer.analyze(staff, shifts, policy)) {
      FatigueScore score =
          scoreRepository.save(
              FatigueScore.builder()
                  .runId(run.getId())
                  .staffId(result.getStaff().getId())
                  .totalPoints(result.getScore().getTotalPoints())
                  .riskLevel(result.getScore().getRiskLevel().name())
                  .churnIndex(result.getChurnIndex())
                  .build());

      for (ViolationDraft draft : result.getViolations()) {
        violationRepository.save(
            Violation.builder()
                .scoreId(score.getId())
                .ruleCode(draft.getRuleCode())
                .severity(draft.getSeverity().name())
                .message(draft.getMessage())
                .evidenceJson(draft.getEvidenceJson())
                .build());
      }
    }
  }

  private List<Shift> applyChanges(List<Shift> base, List<ShiftChangeCommand> changes) {
    Map<Long, Shift> map = new HashMap<>();
    for (Shift s : base) {
      map.put(
          s.getId(),
          Shift.builder()
              .id(s.getId())
              .staffId(s.getStaffId())
              .startAt(s.getStartAt())
              .endAt(s.getEndAt())
              .shiftType(s.getShiftType())
              .revisionCount(s.getRevisionCount())
              .updatedAt(s.getUpdatedAt())
              .build());
    }
    for (ShiftChangeCommand change : changes) {
      Shift shift = map.get(change.getShiftId());
      if (shift != null) {
        shift.setStaffId(change.getNewStaffId());
      }
    }
    return new ArrayList<>(map.values());
  }

  private List<Shift> loadWeekShifts(LocalDate weekStart) {
    return shiftRepository.findByWeek(
        AppTimeZones.startOfWeek(weekStart), AppTimeZones.endOfWeekExclusive(weekStart));
  }

  private FatiguePolicy loadPolicy(Long id) {
    return policyRepository.findById(id).orElseThrow(() -> notFound("Policy", id));
  }

  private Map<Long, List<Violation>> loadViolations(List<FatigueScore> scores) {
    List<Long> scoreIds = scores.stream().map(FatigueScore::getId).toList();
    if (scoreIds.isEmpty()) {
      return Map.of();
    }
    return violationRepository.findByScoreIdIn(scoreIds).stream()
        .collect(Collectors.groupingBy(Violation::getScoreId));
  }

  private NoSuchElementException notFound(String entity, Long id) {
    return new NoSuchElementException(entity + " not found: " + id);
  }
}
