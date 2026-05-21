/* Nhom I */
package com.rotaguard.web.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReassignShiftRequest {

  @NotNull private Long newStaffId;
}
