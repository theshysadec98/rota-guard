/* Nhom I */
package com.rotaguard.web.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class PolicyDiffRequest {

  @NotNull private LocalDate weekStart;
  @NotNull private Long policyIdA;
  @NotNull private Long policyIdB;
}
