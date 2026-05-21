/* Nhom I */
package com.rotaguard.service;

import com.rotaguard.domain.entity.Staff;
import com.rotaguard.domain.enums.StaffListSortDirection;
import com.rotaguard.domain.enums.StaffListSortField;
import com.rotaguard.repository.StaffRepository;
import com.rotaguard.support.CsvParseResult;
import com.rotaguard.support.StaffCsvParser;
import com.rotaguard.web.mapper.WebMapper;
import com.rotaguard.web.request.CreateStaffRequest;
import com.rotaguard.web.request.ImportStaffRequest;
import com.rotaguard.web.request.StaffImportItemRequest;
import com.rotaguard.web.request.UpdateStaffRequest;
import com.rotaguard.web.response.ImportLineError;
import com.rotaguard.web.response.StaffImportResponse;
import com.rotaguard.web.response.StaffPageResponse;
import com.rotaguard.web.response.StaffResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StaffService {

  private final StaffRepository staffRepository;

  @Transactional(readOnly = true)
  public StaffPageResponse search(
      String keyword, String department, String role, int page, int size, String sort, String dir) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Staff> result =
        staffRepository.search(
            keyword,
            department,
            role,
            StaffListSortField.fromParam(sort),
            StaffListSortDirection.fromParam(dir),
            pageable);
    return StaffPageResponse.builder()
        .items(result.getContent().stream().map(WebMapper::toStaffResponse).toList())
        .page(result.getNumber())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .totalPages(result.getTotalPages())
        .build();
  }

  @Transactional
  public StaffResponse create(CreateStaffRequest request) {
    Staff staff =
        staffRepository.save(
            Staff.builder()
                .name(request.getName().trim())
                .role(request.getRole().trim().toUpperCase())
                .department(request.getDepartment().trim())
                .sensitivityFactor(request.getSensitivityFactor())
                .build());
    return WebMapper.toStaffResponse(staff);
  }

  @Transactional
  public StaffResponse update(Long id, UpdateStaffRequest request) {
    Staff staff =
        staffRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Staff not found: " + id));
    staff.setName(request.getName().trim());
    staff.setRole(request.getRole().trim().toUpperCase());
    staff.setDepartment(request.getDepartment().trim());
    staff.setSensitivityFactor(request.getSensitivityFactor());
    return WebMapper.toStaffResponse(staffRepository.save(staff));
  }

  @Transactional
  public StaffImportResponse importStaff(ImportStaffRequest request) {
    return importItems(request.getStaff(), 1);
  }

  @Transactional
  public StaffImportResponse importStaffCsv(String csvContent) {
    CsvParseResult<StaffImportItemRequest> parsed = StaffCsvParser.parse(csvContent);
    if (parsed.hasErrors() && parsed.getRows().isEmpty()) {
      return StaffImportResponse.builder()
          .created(0)
          .updated(0)
          .skipped(0)
          .total(0)
          .errors(parsed.getErrors())
          .build();
    }
    StaffImportResponse result = importItems(parsed.getRows(), 2);
    List<ImportLineError> allErrors = new ArrayList<>(parsed.getErrors());
    if (result.getErrors() != null) {
      allErrors.addAll(result.getErrors());
    }
    return StaffImportResponse.builder()
        .created(result.getCreated())
        .updated(result.getUpdated())
        .skipped(result.getSkipped())
        .total(result.getTotal())
        .errors(allErrors)
        .build();
  }

  private StaffImportResponse importItems(List<StaffImportItemRequest> items, int lineOffset) {
    int created = 0;
    int skipped = 0;
    List<ImportLineError> errors = new ArrayList<>();
    int line = lineOffset;

    for (StaffImportItemRequest item : items) {
      line++;
      try {
        if (item.getName() == null || item.getName().isBlank()) {
          errors.add(ImportLineError.builder().line(line).message("name is required").build());
          skipped++;
          continue;
        }
        staffRepository.save(
            Staff.builder()
                .name(item.getName().trim())
                .role(item.getRole().trim().toUpperCase())
                .department(item.getDepartment().trim())
                .sensitivityFactor(item.getSensitivityFactor())
                .build());
        created++;
      } catch (Exception ex) {
        errors.add(ImportLineError.builder().line(line).message(ex.getMessage()).build());
        skipped++;
      }
    }

    return StaffImportResponse.builder()
        .created(created)
        .updated(0)
        .skipped(skipped)
        .total(created)
        .errors(errors)
        .build();
  }
}
