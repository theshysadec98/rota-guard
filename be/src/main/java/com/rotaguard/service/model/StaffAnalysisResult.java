/* Nhom I */
package com.rotaguard.service.model;

import com.rotaguard.domain.entity.Staff;
import com.rotaguard.rule.model.ViolationDraft;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StaffAnalysisResult {
  Staff staff;
  ScoreResult score;
  List<ViolationDraft> violations;
  int churnIndex;
}
