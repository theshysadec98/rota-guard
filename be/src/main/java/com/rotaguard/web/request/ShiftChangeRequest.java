/* Nhom I */
package com.rotaguard.web.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShiftChangeRequest {

  @NotNull private Long shiftId;
  @NotNull private Long newStaffId;
}
