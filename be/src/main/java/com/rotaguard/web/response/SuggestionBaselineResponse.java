/* Nhom I */
package com.rotaguard.web.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SuggestionBaselineResponse {
  int redCount;
  int yellowCount;
  int greenCount;
}
