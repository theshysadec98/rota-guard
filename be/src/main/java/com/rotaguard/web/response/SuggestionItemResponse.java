/* Nhom I */
package com.rotaguard.web.response;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SuggestionItemResponse {
  int rank;
  String action;
  long shiftId;
  long fromStaffId;
  String fromStaffName;
  long toStaffId;
  String toStaffName;
  String shiftSummary;
  List<String> reasonCodes;
  SuggestionImpactResponse impact;
  String explanation;
}
