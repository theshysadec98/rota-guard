/* Nhom I */
package com.rotaguard.web.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PolicyDiffResponse {
  LocalDate weekStart;
  Long policyIdA;
  Long policyIdB;
  long additionalRedCount;
  List<PolicyDiffEntryResponse> diffs;
}
