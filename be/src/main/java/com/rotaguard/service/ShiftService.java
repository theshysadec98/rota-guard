/* Nhom I */
package com.rotaguard.service;

import com.rotaguard.domain.entity.Shift;
import com.rotaguard.domain.entity.ShiftRevision;
import com.rotaguard.repository.ShiftRepository;
import com.rotaguard.repository.ShiftRevisionRepository;
import com.rotaguard.repository.StaffRepository;
import com.rotaguard.support.AppTimeZones;
import com.rotaguard.support.CsvParseResult;
import com.rotaguard.support.ShiftCsvParser;
import com.rotaguard.support.WeekUtils;
import com.rotaguard.web.request.ImportShiftsRequest;
import com.rotaguard.web.request.ShiftImportItemRequest;
import com.rotaguard.web.response.ImportLineError;
import com.rotaguard.web.response.ShiftImportResponse;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShiftService {

  private final ShiftRepository shiftRepository;
  private final ShiftRevisionRepository shiftRevisionRepository;
  private final StaffRepository staffRepository;

  @Transactional
  public ShiftImportResponse importShifts(ImportShiftsRequest request) {
    WeekUtils.requireMonday(request.getWeekStart());
    if (request.isReplaceWeek()) {
      deleteWeekShifts(request.getWeekStart());
    }

    int created = 0;
    int updated = 0;
    List<ImportLineError> errors = new ArrayList<>();

    int line = 1;
    for (ShiftImportItemRequest item : request.getShifts()) {
      line++;
      try {
        validateShiftItem(item);
        if (item.getId() != null) {
          updated += updateShift(item);
        } else {
          Shift existing = findByStaffAndStart(item.getStaffId(), item.getStartAt());
          if (existing != null) {
            updated += updateShift(item.getStaffId(), item.getStartAt(), item);
          } else {
            created += createShift(item);
          }
        }
      } catch (IllegalArgumentException ex) {
        errors.add(ImportLineError.builder().line(line).message(ex.getMessage()).build());
      }
    }

    return ShiftImportResponse.builder()
        .created(created)
        .updated(updated)
        .total(created + updated)
        .errors(errors)
        .build();
  }

  @Transactional
  public ShiftImportResponse importShiftsCsv(
      LocalDate weekStart, boolean replaceWeek, String csvContent) {
    WeekUtils.requireMonday(weekStart);
    CsvParseResult<ShiftImportItemRequest> parsed = ShiftCsvParser.parse(csvContent);
    if (parsed.hasErrors() && parsed.getRows().isEmpty()) {
      return ShiftImportResponse.builder()
          .created(0)
          .updated(0)
          .total(0)
          .errors(parsed.getErrors())
          .build();
    }

    ImportShiftsRequest request = new ImportShiftsRequest();
    request.setWeekStart(weekStart);
    request.setReplaceWeek(replaceWeek);
    request.setShifts(parsed.getRows());

    ShiftImportResponse result = importShifts(request);
    List<ImportLineError> allErrors = new ArrayList<>(parsed.getErrors());
    if (result.getErrors() != null) {
      allErrors.addAll(result.getErrors());
    }
    return ShiftImportResponse.builder()
        .created(result.getCreated())
        .updated(result.getUpdated())
        .total(result.getTotal())
        .errors(allErrors)
        .build();
  }

  private void validateShiftItem(ShiftImportItemRequest item) {
    if (!staffRepository.existsById(item.getStaffId())) {
      throw new IllegalArgumentException("Staff not found: " + item.getStaffId());
    }
    if (!item.getEndAt().isAfter(item.getStartAt())) {
      throw new IllegalArgumentException("endAt must be after startAt");
    }
  }

  private int createShift(ShiftImportItemRequest item) {
    Shift shift =
        shiftRepository.save(
            Shift.builder()
                .staffId(item.getStaffId())
                .startAt(item.getStartAt())
                .endAt(item.getEndAt())
                .shiftType(item.getShiftType().name())
                .revisionCount(0)
                .updatedAt(AppTimeZones.now())
                .build());
    recordRevision(shift.getId(), "IMPORT_CREATE");
    return 1;
  }

  private int updateShift(ShiftImportItemRequest item) {
    Shift shift =
        shiftRepository
            .findById(item.getId())
            .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + item.getId()));
    applyItem(shift, item);
    shift.setRevisionCount(shift.getRevisionCount() + 1);
    shift.setUpdatedAt(AppTimeZones.now());
    shiftRepository.save(shift);
    recordRevision(shift.getId(), "IMPORT_UPDATE");
    return 1;
  }

  private int updateShift(Long staffId, ZonedDateTime startAt, ShiftImportItemRequest item) {
    Shift shift = findByStaffAndStart(staffId, startAt);
    applyItem(shift, item);
    shift.setRevisionCount(shift.getRevisionCount() + 1);
    shift.setUpdatedAt(AppTimeZones.now());
    shiftRepository.save(shift);
    recordRevision(shift.getId(), "IMPORT_UPDATE");
    return 1;
  }

  private void applyItem(Shift shift, ShiftImportItemRequest item) {
    validateShiftItem(item);
    shift.setStaffId(item.getStaffId());
    shift.setStartAt(item.getStartAt());
    shift.setEndAt(item.getEndAt());
    shift.setShiftType(item.getShiftType().name());
  }

  private Shift findByStaffAndStart(Long staffId, ZonedDateTime startAt) {
    ZonedDateTime key = startAt.withZoneSameInstant(AppTimeZones.APP_ZONE);
    return shiftRepository.findByStaffIdOrderByStartAtAsc(staffId).stream()
        .filter(s -> s.getStartAt().withZoneSameInstant(AppTimeZones.APP_ZONE).equals(key))
        .findFirst()
        .orElse(null);
  }

  private void deleteWeekShifts(LocalDate weekStart) {
    List<Shift> existing =
        shiftRepository.findByWeek(
            AppTimeZones.startOfWeek(weekStart), AppTimeZones.endOfWeekExclusive(weekStart));
    if (existing.isEmpty()) {
      return;
    }
    List<Long> shiftIds = existing.stream().map(Shift::getId).toList();
    shiftRevisionRepository.deleteByShiftIdIn(shiftIds);
    shiftRepository.deleteAll(existing);
  }

  @Transactional
  public void deleteShift(Long shiftId) {
    Shift shift =
        shiftRepository
            .findById(shiftId)
            .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + shiftId));
    shiftRevisionRepository.deleteByShiftIdIn(List.of(shift.getId()));
    shiftRepository.delete(shift);
  }

  @Transactional
  public Shift reassign(Long shiftId, Long newStaffId) {
    Shift shift =
        shiftRepository
            .findById(shiftId)
            .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + shiftId));
    if (!staffRepository.existsById(newStaffId)) {
      throw new IllegalArgumentException("Staff not found: " + newStaffId);
    }
    shift.setStaffId(newStaffId);
    shift.setRevisionCount(shift.getRevisionCount() + 1);
    shift.setUpdatedAt(AppTimeZones.now());
    shiftRepository.save(shift);
    recordRevision(shiftId, "REASSIGN");
    return shift;
  }

  private void recordRevision(Long shiftId, String changeType) {
    shiftRevisionRepository.save(
        ShiftRevision.builder().shiftId(shiftId).changeType(changeType).build());
  }
}
