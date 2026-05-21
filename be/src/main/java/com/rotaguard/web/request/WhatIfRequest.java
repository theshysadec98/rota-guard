/* Nhom I */
package com.rotaguard.web.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class WhatIfRequest {

  @NotNull private LocalDate weekStart;
  @NotNull private Long policyId;
  @NotNull private List<ShiftChangeRequest> shiftChanges;
}
