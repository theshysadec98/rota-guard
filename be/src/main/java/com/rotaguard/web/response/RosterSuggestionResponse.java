/* Nhom I */
package com.rotaguard.web.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RosterSuggestionResponse {
  LocalDate weekStart;
  Long policyId;
  SuggestionBaselineResponse baseline;
  List<SuggestionItemResponse> suggestions;
  String warning;
}
