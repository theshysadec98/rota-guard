/* Nhom I */
package com.rotaguard.web.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PolicyResponse {
  Long id;
  String name;
  String department;
  double minRestHours;
  int maxConsecutiveNights;
  double maxWeeklyHours;
  int churnThreshold;
}
