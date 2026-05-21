/* Nhom I */
package com.rotaguard.service;

import com.rotaguard.domain.entity.Shift;
import com.rotaguard.domain.entity.Staff;
import com.rotaguard.domain.enums.RiskLevel;
import com.rotaguard.repository.ShiftRepository;
import com.rotaguard.repository.StaffRepository;
import com.rotaguard.rule.model.ViolationDraft;
import com.rotaguard.service.model.ShiftChangeCommand;
import com.rotaguard.service.model.StaffAnalysisResult;
import com.rotaguard.support.AppTimeZones;
import com.rotaguard.web.request.RosterSuggestionRequest;
import com.rotaguard.web.response.RosterSuggestionResponse;
import com.rotaguard.web.response.SuggestionBaselineResponse;
import com.rotaguard.web.response.SuggestionImpactResponse;
import com.rotaguard.web.response.SuggestionItemResponse;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RosterSuggestionService {

  private static final int MAX_CANDIDATES_PER_SHIFT = 20;
  private static final int MAX_SIMULATIONS = 500;
  private static final DateTimeFormatter SHIFT_DAY =
      DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(AppTimeZones.APP_ZONE);

  private final AnalysisService analysisService;
  private final StaffRepository staffRepository;
  private final ShiftRepository shiftRepository;

  @Transactional(readOnly = true)
  public RosterSuggestionResponse suggest(RosterSuggestionRequest request) {
    LocalDate weekStart = request.getWeekStart();
    Long policyId = request.getPolicyId();
    Set<String> targetLevels =
        new HashSet<>(
            request.getTargetRiskLevels() != null && !request.getTargetRiskLevels().isEmpty()
                ? request.getTargetRiskLevels()
                : List.of("RED"));
    int maxSuggestions = request.getMaxSuggestions() != null ? request.getMaxSuggestions() : 10;

    List<Staff> staff = staffRepository.findAll();
    Map<Long, Staff> staffById =
        staff.stream().collect(Collectors.toMap(Staff::getId, s -> s, (a, b) -> a));
    List<Shift> shifts = loadWeekShifts(weekStart);

    Map<Long, StaffAnalysisResult> before = analysisService.analyzeWeek(weekStart, policyId);
    SuggestionBaselineResponse baseline = toBaseline(before);

    List<Long> targetStaffIds =
        staff.stream()
            .map(Staff::getId)
            .filter(before::containsKey)
            .filter(id -> targetLevels.contains(before.get(id).getScore().getRiskLevel().name()))
            .toList();

    List<ScoredCandidate> ranked = new ArrayList<>();
    int simulations = 0;
    String warning = null;

    for (Long targetId : targetStaffIds) {
      Staff target = staffById.get(targetId);
      if (target == null) {
        continue;
      }
      StaffAnalysisResult targetBefore = before.get(targetId);
      Set<String> reasonCodes =
          targetBefore.getViolations().stream()
              .map(ViolationDraft::getRuleCode)
              .collect(Collectors.toCollection(LinkedHashSet::new));

      List<Shift> targetShifts =
          shifts.stream().filter(s -> s.getStaffId().equals(targetId)).toList();

      for (Shift shift : targetShifts) {
        List<Staff> candidates =
            staff.stream()
                .filter(c -> !c.getId().equals(targetId))
                .filter(c -> c.getDepartment().equals(target.getDepartment()))
                .limit(MAX_CANDIDATES_PER_SHIFT)
                .toList();

        for (Staff candidate : candidates) {
          if (simulations >= MAX_SIMULATIONS) {
            warning = "Đã đạt giới hạn mô phỏng; kết quả có thể chưa đầy đủ.";
            break;
          }
          if (hasOverlap(candidate.getId(), shift, shifts)) {
            continue;
          }

          simulations++;
          Map<Long, StaffAnalysisResult> after =
              analysisService.analyzeWeekWithChanges(
                  weekStart,
                  policyId,
                  List.of(
                      ShiftChangeCommand.builder()
                          .shiftId(shift.getId())
                          .newStaffId(candidate.getId())
                          .build()));

          int score = scoreSuggestion(before, after, targetId, candidate.getId());
          if (score <= 0) {
            continue;
          }

          StaffAnalysisResult tAfter = after.get(targetId);
          StaffAnalysisResult cAfter = after.get(candidate.getId());
          int redBefore = countRisk(before, RiskLevel.RED);
          int redAfter = countRisk(after, RiskLevel.RED);

          SuggestionImpactResponse impact =
              SuggestionImpactResponse.builder()
                  .targetRiskBefore(targetBefore.getScore().getRiskLevel().name())
                  .targetRiskAfter(tAfter.getScore().getRiskLevel().name())
                  .targetPointsDelta(
                      tAfter.getScore().getTotalPoints() - targetBefore.getScore().getTotalPoints())
                  .redCountBefore(redBefore)
                  .redCountAfter(redAfter)
                  .build();

          ranked.add(
              new ScoredCandidate(
                  score, shift, target, candidate, new ArrayList<>(reasonCodes), impact));
        }
        if (warning != null) {
          break;
        }
      }
      if (warning != null) {
        break;
      }
    }

    ranked.sort(Comparator.comparingInt(ScoredCandidate::score).reversed());

    Set<String> seen = new HashSet<>();
    List<SuggestionItemResponse> suggestions = new ArrayList<>();
    int rank = 1;
    for (ScoredCandidate c : ranked) {
      String key = c.shift().getId() + ":" + c.candidate().getId();
      if (!seen.add(key)) {
        continue;
      }
      suggestions.add(toItem(rank++, c));
      if (suggestions.size() >= maxSuggestions) {
        break;
      }
    }

    return RosterSuggestionResponse.builder()
        .weekStart(weekStart)
        .policyId(policyId)
        .baseline(baseline)
        .suggestions(suggestions)
        .warning(warning)
        .build();
  }

  private int scoreSuggestion(
      Map<Long, StaffAnalysisResult> before,
      Map<Long, StaffAnalysisResult> after,
      long targetId,
      long candidateId) {
    StaffAnalysisResult tBefore = before.get(targetId);
    StaffAnalysisResult tAfter = after.get(targetId);
    StaffAnalysisResult cBefore = before.get(candidateId);
    StaffAnalysisResult cAfter = after.get(candidateId);

    int redBefore = countRisk(before, RiskLevel.RED);
    int redAfter = countRisk(after, RiskLevel.RED);

    int score = 0;
    if (tBefore.getScore().getRiskLevel() == RiskLevel.RED
        && tAfter.getScore().getRiskLevel() != RiskLevel.RED) {
      score += 1000;
    }
    score += 100 * (tBefore.getScore().getTotalPoints() - tAfter.getScore().getTotalPoints());
    score += 50 * (redBefore - redAfter);
    if (cBefore.getScore().getRiskLevel() != RiskLevel.RED
        && cAfter.getScore().getRiskLevel() == RiskLevel.RED) {
      score -= 500;
    }
    return score;
  }

  private SuggestionItemResponse toItem(int rank, ScoredCandidate c) {
    Shift shift = c.shift();
    String shiftSummary =
        SHIFT_DAY.format(shift.getStartAt())
            + " "
            + shift.getShiftType()
            + " "
            + c.target().getDepartment();

    return SuggestionItemResponse.builder()
        .rank(rank)
        .action("REASSIGN_SHIFT")
        .shiftId(shift.getId())
        .fromStaffId(c.target().getId())
        .fromStaffName(c.target().getName())
        .toStaffId(c.candidate().getId())
        .toStaffName(c.candidate().getName())
        .shiftSummary(shiftSummary)
        .reasonCodes(c.reasonCodes())
        .impact(c.impact())
        .explanation(buildExplanation(c))
        .build();
  }

  private String buildExplanation(ScoredCandidate c) {
    SuggestionImpactResponse impact = c.impact();
    String rules =
        c.reasonCodes().isEmpty()
            ? "vi phạm mệt mỏi"
            : c.reasonCodes().stream().map(this::ruleLabel).collect(Collectors.joining(", "));

    String targetChange =
        impact.getTargetRiskBefore().equals(impact.getTargetRiskAfter())
            ? String.format(
                "%s vẫn %s nhưng giảm %d điểm",
                c.target().getName(), impact.getTargetRiskAfter(), -impact.getTargetPointsDelta())
            : String.format(
                "%s: %s → %s",
                c.target().getName(), impact.getTargetRiskBefore(), impact.getTargetRiskAfter());

    return String.format(
        Locale.forLanguageTag("vi"),
        "Đổi ca %s cho %s (%s). %s. Còn %d RED trong tuần (trước %d).",
        shiftLabel(c.shift()),
        c.candidate().getName(),
        rules,
        targetChange,
        impact.getRedCountAfter(),
        impact.getRedCountBefore());
  }

  private String shiftLabel(Shift shift) {
    return shift.getShiftType().equals("NIGHT") ? "đêm" : "ngày";
  }

  private String ruleLabel(String code) {
    return switch (code) {
      case "QUICK_RETURN" -> "nghỉ ngắn";
      case "CONSECUTIVE_NIGHT" -> "đêm liên tiếp";
      case "WEEKLY_HOURS" -> "giờ tuần";
      default -> code.toLowerCase(Locale.ROOT);
    };
  }

  private boolean hasOverlap(long staffId, Shift candidateShift, List<Shift> allShifts) {
    for (Shift other : allShifts) {
      if (!other.getStaffId().equals(staffId) || other.getId().equals(candidateShift.getId())) {
        continue;
      }
      if (overlaps(
          candidateShift.getStartAt(),
          candidateShift.getEndAt(),
          other.getStartAt(),
          other.getEndAt())) {
        return true;
      }
    }
    return false;
  }

  private boolean overlaps(
      ZonedDateTime aStart, ZonedDateTime aEnd, ZonedDateTime bStart, ZonedDateTime bEnd) {
    return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
  }

  private int countRisk(Map<Long, StaffAnalysisResult> map, RiskLevel level) {
    return (int) map.values().stream().filter(r -> r.getScore().getRiskLevel() == level).count();
  }

  private SuggestionBaselineResponse toBaseline(Map<Long, StaffAnalysisResult> map) {
    return SuggestionBaselineResponse.builder()
        .redCount(countRisk(map, RiskLevel.RED))
        .yellowCount(countRisk(map, RiskLevel.YELLOW))
        .greenCount(countRisk(map, RiskLevel.GREEN))
        .build();
  }

  private List<Shift> loadWeekShifts(LocalDate weekStart) {
    return shiftRepository.findByWeek(
        AppTimeZones.startOfWeek(weekStart), AppTimeZones.endOfWeekExclusive(weekStart));
  }

  private record ScoredCandidate(
      int score,
      Shift shift,
      Staff target,
      Staff candidate,
      List<String> reasonCodes,
      SuggestionImpactResponse impact) {}
}
