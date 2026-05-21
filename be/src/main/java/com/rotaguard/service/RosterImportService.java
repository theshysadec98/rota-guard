/* Nhom I */
package com.rotaguard.service;

import com.rotaguard.domain.entity.Staff;
import com.rotaguard.repository.StaffRepository;
import com.rotaguard.support.RosterExcelParser;
import com.rotaguard.support.WeekUtils;
import com.rotaguard.web.request.ImportShiftsRequest;
import com.rotaguard.web.request.ShiftImportItemRequest;
import com.rotaguard.web.response.ImportLineError;
import com.rotaguard.web.response.RosterImportResponse;
import com.rotaguard.web.response.ShiftImportResponse;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RosterImportService {

  private final StaffRepository staffRepository;
  private final ShiftService shiftService;

  @Transactional
  public RosterImportResponse importExcel(
      InputStream input, LocalDate weekStartParam, boolean replaceWeek) {
    RosterExcelParser.ParseResult parsed = RosterExcelParser.parse(input, weekStartParam);

    List<ImportLineError> errors = new ArrayList<>(parsed.errors());
    LocalDate weekStart = parsed.weekStart();
    if (weekStart == null) {
      return RosterImportResponse.builder()
          .weekStart(null)
          .staffCreated(0)
          .staffMatched(0)
          .shiftsCreated(0)
          .shiftsUpdated(0)
          .shiftRows(0)
          .errors(errors)
          .build();
    }

    try {
      WeekUtils.requireMonday(weekStart);
    } catch (IllegalArgumentException ex) {
      errors.add(ImportLineError.builder().line(1).message(ex.getMessage()).build());
      return RosterImportResponse.builder()
          .weekStart(weekStart)
          .staffCreated(0)
          .staffMatched(0)
          .shiftsCreated(0)
          .shiftsUpdated(0)
          .shiftRows(0)
          .errors(errors)
          .build();
    }

    int staffCreated = 0;
    int staffMatched = 0;
    Map<String, Long> staffIds = new HashMap<>();

    for (RosterExcelParser.ParsedStaff row : parsed.staffRows()) {
      String key = staffKey(row.name(), row.department());
      if (staffIds.containsKey(key)) {
        continue;
      }
      var existing =
          staffRepository.findByNameIgnoreCaseAndDepartment(row.name(), row.department());
      if (existing.isPresent()) {
        staffIds.put(key, existing.get().getId());
        staffMatched++;
      } else {
        Staff created =
            staffRepository.save(
                Staff.builder()
                    .name(row.name())
                    .role(row.role())
                    .department(row.department())
                    .sensitivityFactor(1.0)
                    .build());
        staffIds.put(key, created.getId());
        staffCreated++;
      }
    }

    List<ShiftImportItemRequest> shiftItems = new ArrayList<>();
    for (RosterExcelParser.ParsedShift shift : parsed.shifts()) {
      Long staffId = staffIds.get(staffKey(shift.staff().name(), shift.staff().department()));
      if (staffId == null) {
        var existing =
            staffRepository.findByNameIgnoreCaseAndDepartment(
                shift.staff().name(), shift.staff().department());
        if (existing.isPresent()) {
          staffId = existing.get().getId();
          staffIds.put(staffKey(shift.staff().name(), shift.staff().department()), staffId);
        } else {
          Staff created =
              staffRepository.save(
                  Staff.builder()
                      .name(shift.staff().name())
                      .role(shift.staff().role())
                      .department(shift.staff().department())
                      .sensitivityFactor(1.0)
                      .build());
          staffId = created.getId();
          staffIds.put(staffKey(shift.staff().name(), shift.staff().department()), staffId);
          staffCreated++;
        }
      }
      ShiftImportItemRequest item = new ShiftImportItemRequest();
      item.setStaffId(staffId);
      item.setStartAt(shift.startAt());
      item.setEndAt(shift.endAt());
      item.setShiftType(shift.shiftType());
      shiftItems.add(item);
    }

    int shiftsCreated = 0;
    int shiftsUpdated = 0;
    if (!shiftItems.isEmpty()) {
      ImportShiftsRequest request = new ImportShiftsRequest();
      request.setWeekStart(weekStart);
      request.setReplaceWeek(replaceWeek);
      request.setShifts(shiftItems);
      ShiftImportResponse shiftResult = shiftService.importShifts(request);
      shiftsCreated = shiftResult.getCreated();
      shiftsUpdated = shiftResult.getUpdated();
      if (shiftResult.getErrors() != null) {
        errors.addAll(shiftResult.getErrors());
      }
    }

    return RosterImportResponse.builder()
        .weekStart(weekStart)
        .staffCreated(staffCreated)
        .staffMatched(staffMatched)
        .shiftsCreated(shiftsCreated)
        .shiftsUpdated(shiftsUpdated)
        .shiftRows(shiftItems.size())
        .errors(errors)
        .build();
  }

  private static String staffKey(String name, String department) {
    return name.trim().toLowerCase() + "|" + department.trim().toLowerCase();
  }
}
