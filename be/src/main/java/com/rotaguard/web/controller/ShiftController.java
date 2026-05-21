/* Nhom I */
package com.rotaguard.web.controller;

import com.rotaguard.repository.ShiftRepository;
import com.rotaguard.service.ShiftService;
import com.rotaguard.support.AppTimeZones;
import com.rotaguard.support.WeekUtils;
import com.rotaguard.web.mapper.WebMapper;
import com.rotaguard.web.request.ImportShiftsRequest;
import com.rotaguard.web.request.ReassignShiftRequest;
import com.rotaguard.web.response.ShiftImportResponse;
import com.rotaguard.web.response.ShiftResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Shifts")
@RestController
@RequestMapping("/api/v1/shifts")
@Validated
@RequiredArgsConstructor
public class ShiftController {

  private final ShiftRepository shiftRepository;
  private final ShiftService shiftService;

  @Operation(summary = "List shifts for ISO week starting weekStart")
  @GetMapping
  public List<ShiftResponse> listShifts(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate weekStart) {
    WeekUtils.requireMonday(weekStart);
    return shiftRepository
        .findByWeek(AppTimeZones.startOfWeek(weekStart), AppTimeZones.endOfWeekExclusive(weekStart))
        .stream()
        .map(WebMapper::toShiftResponse)
        .toList();
  }

  @Operation(summary = "Import shifts (JSON); increments revision_count on updates")
  @PostMapping("/import")
  @ResponseStatus(HttpStatus.CREATED)
  public ShiftImportResponse importShifts(@Valid @RequestBody ImportShiftsRequest request) {
    return shiftService.importShifts(request);
  }

  @Operation(summary = "Reassign shift to another staff member")
  @PostMapping("/{id}/reassign")
  public ShiftResponse reassign(
      @PathVariable Long id, @Valid @RequestBody ReassignShiftRequest request) {
    return WebMapper.toShiftResponse(shiftService.reassign(id, request.getNewStaffId()));
  }

  @Operation(summary = "Import shifts from CSV file")
  @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ShiftImportResponse importShiftsCsv(
      @RequestParam("file") MultipartFile file,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate weekStart,
      @RequestParam(defaultValue = "false") boolean replaceWeek)
      throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("file is required");
    }
    String content = new String(file.getBytes(), StandardCharsets.UTF_8);
    return shiftService.importShiftsCsv(weekStart, replaceWeek, content);
  }
}
