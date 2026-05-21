/* Nhom I */
package com.rotaguard.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class ImportStaffRequest {

  @NotEmpty @Valid private List<StaffImportItemRequest> staff;
}
