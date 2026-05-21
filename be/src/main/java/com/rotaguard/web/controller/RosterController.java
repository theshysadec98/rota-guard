/* Nhom I */
package com.rotaguard.web.controller;

import com.rotaguard.service.RosterImportService;
import com.rotaguard.web.response.RosterImportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Roster")
@RestController
@RequestMapping("/api/v1/roster")
@RequiredArgsConstructor
public class RosterController {

  private final RosterImportService rosterImportService;

  @Operation(summary = "Import weekly roster from Excel (.xlsx)")
  @PostMapping(value = "/import/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public RosterImportResponse importExcel(
      @RequestParam("file") MultipartFile file,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate weekStart,
      @RequestParam(defaultValue = "true") boolean replaceWeek)
      throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("file is required");
    }
    String name = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
    if (!name.toLowerCase().endsWith(".xlsx") && !name.toLowerCase().endsWith(".xls")) {
      throw new IllegalArgumentException("Only .xlsx or .xls files are supported");
    }
    return rosterImportService.importExcel(file.getInputStream(), weekStart, replaceWeek);
  }
}
