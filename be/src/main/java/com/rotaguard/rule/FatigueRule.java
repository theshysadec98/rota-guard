/* Nhom I */
package com.rotaguard.rule;

import com.rotaguard.domain.entity.FatiguePolicy;
import com.rotaguard.domain.entity.Shift;
import com.rotaguard.domain.entity.Staff;
import com.rotaguard.rule.model.ViolationDraft;
import java.util.List;

public interface FatigueRule {

  String code();

  List<ViolationDraft> evaluate(Staff staff, List<Shift> shifts, FatiguePolicy policy);
}
