/* Nhom I */
package com.rotaguard.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class ImportShiftsRequest {

  @NotNull private LocalDate weekStart;

  private boolean replaceWeek = false;

  @NotEmpty @Valid private List<ShiftImportItemRequest> shifts;
}
