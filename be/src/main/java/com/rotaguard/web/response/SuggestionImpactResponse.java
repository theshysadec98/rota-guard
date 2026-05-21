/* Nhom I */
package com.rotaguard.web.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SuggestionImpactResponse {
  String targetRiskBefore;
  String targetRiskAfter;
  int targetPointsDelta;
  int redCountBefore;
  int redCountAfter;
}
