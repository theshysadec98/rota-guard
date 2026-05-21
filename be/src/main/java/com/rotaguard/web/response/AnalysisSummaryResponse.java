/* Nhom I */
package com.rotaguard.web.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AnalysisSummaryResponse {
  int greenCount;
  int yellowCount;
  int redCount;
}
