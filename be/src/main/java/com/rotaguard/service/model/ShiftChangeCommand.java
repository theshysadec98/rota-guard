/* Nhom I */
package com.rotaguard.service.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShiftChangeCommand {
  Long shiftId;
  Long newStaffId;
}
