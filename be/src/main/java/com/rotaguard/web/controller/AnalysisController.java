/* Nhom I */
package com.rotaguard.web.controller;

import com.rotaguard.service.AnalysisService;
import com.rotaguard.service.RosterSuggestionService;
import com.rotaguard.web.mapper.WebMapper;
import com.rotaguard.web.request.PolicyDiffRequest;
import com.rotaguard.web.request.RosterSuggestionRequest;
import com.rotaguard.web.request.WhatIfRequest;
import com.rotaguard.web.response.AnalysisReportResponse;
import com.rotaguard.web.response.PolicyDiffResponse;
import com.rotaguard.web.response.RosterSuggestionResponse;
import com.rotaguard.web.response.RunAnalysisResponse;
import com.rotaguard.web.response.WhatIfResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Analysis", description = "Shift fatigue analysis")
@RestController
@RequestMapping("/api/v1/analysis")
@Validated
@RequiredArgsConstructor
public class AnalysisController {

  private final AnalysisService analysisService;
  private final RosterSuggestionService rosterSuggestionService;

  @Operation(summary = "Run weekly analysis")
  @PostMapping("/run")
  @ResponseStatus(HttpStatus.CREATED)
  public RunAnalysisResponse runAnalysis(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate weekStart,
      @RequestParam @NotNull Long policyId) {
    return WebMapper.toRunResponse(analysisService.runAnalysis(weekStart, policyId));
  }

  @Operation(summary = "Analysis report by run id")
  @GetMapping("/{runId}/report")
  public AnalysisReportResponse report(@PathVariable Long runId) {
    return analysisService.getReport(runId);
  }

  @Operation(summary = "Compare two fatigue policies")
  @PostMapping("/policy-diff")
  public PolicyDiffResponse policyDiff(@Valid @RequestBody PolicyDiffRequest request) {
    return analysisService.policyDiff(
        request.getWeekStart(), request.getPolicyIdA(), request.getPolicyIdB());
  }

  @Operation(summary = "What-if shift reassignment preview")
  @PostMapping("/what-if")
  public WhatIfResponse whatIf(@Valid @RequestBody WhatIfRequest request) {
    var changes = request.getShiftChanges().stream().map(WebMapper::toCommand).toList();
    return analysisService.whatIf(request.getWeekStart(), request.getPolicyId(), changes);
  }

  @Operation(summary = "Roster change suggestions after analysis")
  @PostMapping("/suggestions")
  public RosterSuggestionResponse suggestions(@Valid @RequestBody RosterSuggestionRequest request) {
    return rosterSuggestionService.suggest(request);
  }
}
