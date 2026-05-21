/* Nhom I */
package com.rotaguard.web.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WhatIfResponse {
  LocalDate weekStart;
  Long policyId;
  List<WhatIfStaffDeltaResponse> deltas;
}
