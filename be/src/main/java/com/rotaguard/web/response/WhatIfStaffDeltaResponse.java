/* Nhom I */
package com.rotaguard.web.response;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WhatIfStaffDeltaResponse {
  Long staffId;
  String staffName;
  String riskBefore;
  String riskAfter;
  int violationsBefore;
  int violationsAfter;
  List<String> changes;
}
