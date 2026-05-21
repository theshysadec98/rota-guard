/* Nhom I */
package com.rotaguard.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class RosterSuggestionRequest {

  @NotNull private LocalDate weekStart;
  @NotNull private Long policyId;
  private Long runId;
  private List<String> targetRiskLevels;

  @Min(1)
  @Max(20)
  private Integer maxSuggestions;
}
