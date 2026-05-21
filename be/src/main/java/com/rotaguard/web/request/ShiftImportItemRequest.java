/* Nhom I */
package com.rotaguard.web.request;

import com.rotaguard.domain.enums.ShiftType;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class ShiftImportItemRequest {

  private Long id;

  @NotNull private Long staffId;

  @NotNull private ZonedDateTime startAt;

  @NotNull private ZonedDateTime endAt;

  @NotNull private ShiftType shiftType;
}
