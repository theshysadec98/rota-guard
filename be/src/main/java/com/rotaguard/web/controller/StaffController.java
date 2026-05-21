/* Nhom I */
package com.rotaguard.web.controller;

import com.rotaguard.service.AnalysisService;
import com.rotaguard.service.StaffService;
import com.rotaguard.web.request.CreateStaffRequest;
import com.rotaguard.web.request.ImportStaffRequest;
import com.rotaguard.web.request.UpdateStaffRequest;
import com.rotaguard.web.response.StaffImportResponse;
import com.rotaguard.web.response.StaffPageResponse;
import com.rotaguard.web.response.StaffResponse;
import com.rotaguard.web.response.StaffRiskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Staff")
@RestController
@RequestMapping("/api/v1/staff")
@Validated
@RequiredArgsConstructor
public class StaffController {

  private final AnalysisService analysisService;
  private final StaffService staffService;

  @Operation(summary = "Search staff with sort and pagination")
  @GetMapping
  public StaffPageResponse listStaff(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String department,
      @RequestParam(required = false) String role,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size,
      @RequestParam(defaultValue = "name") String sort,
      @RequestParam(defaultValue = "asc") String dir) {
    return staffService.search(q, department, role, page, size, sort, dir);
  }

  @Operation(summary = "Create staff member")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public StaffResponse createStaff(@Valid @RequestBody CreateStaffRequest request) {
    return staffService.create(request);
  }

  @Operation(summary = "Update staff member")
  @PutMapping("/{staffId}")
  public StaffResponse updateStaff(
      @PathVariable Long staffId, @Valid @RequestBody UpdateStaffRequest request) {
    return staffService.update(staffId, request);
  }

  @Operation(summary = "Bulk import staff (JSON)")
  @PostMapping("/import")
  @ResponseStatus(HttpStatus.CREATED)
  public StaffImportResponse importStaff(@Valid @RequestBody ImportStaffRequest request) {
    return staffService.importStaff(request);
  }

  @Operation(summary = "Import staff from CSV file")
  @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public StaffImportResponse importStaffCsv(@RequestParam("file") MultipartFile file)
      throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("file is required");
    }
    String content = new String(file.getBytes(), StandardCharsets.UTF_8);
    return staffService.importStaffCsv(content);
  }

  @Operation(summary = "Staff risk detail for an analysis run")
  @GetMapping("/{staffId}/risk")
  public StaffRiskResponse staffRisk(
      @PathVariable Long staffId, @RequestParam @NotNull Long runId) {
    return analysisService.getStaffRisk(runId, staffId);
  }
}
