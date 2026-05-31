/* Nhom I */
package com.rotaguard.service;

import com.rotaguard.domain.entity.Shift;
import com.rotaguard.domain.entity.Staff;
import com.rotaguard.domain.enums.SlotKind;
import com.rotaguard.repository.ShiftRepository;
import com.rotaguard.repository.StaffRepository;
import com.rotaguard.support.AppTimeZones;
import com.rotaguard.support.ShiftSlotMapper;
import com.rotaguard.support.WeekUtils;
import com.rotaguard.web.response.BoardShiftCardResponse;
import com.rotaguard.web.response.WeekBoardDayResponse;
import com.rotaguard.web.response.WeekBoardResponse;
import com.rotaguard.web.response.WeekBoardSummaryResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeekBoardService {

  private static final List<String> DAY_LABELS = List.of("T2", "T3", "T4", "T5", "T6", "T7", "CN");

  private final ShiftRepository shiftRepository;
  private final StaffRepository staffRepository;

  public WeekBoardResponse buildBoard(LocalDate weekStart) {
    WeekUtils.requireMonday(weekStart);
    LocalDate weekEnd = weekStart.plusDays(6);

    List<Shift> shifts =
        shiftRepository.findByWeek(
            AppTimeZones.startOfWeek(weekStart), AppTimeZones.endOfWeekExclusive(weekStart));

    Map<Long, Staff> staffById = loadStaff(shifts);

    List<WeekBoardDayResponse> days = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      LocalDate date = weekStart.plusDays(i);
      days.add(
          WeekBoardDayResponse.builder()
              .date(date)
              .label(DAY_LABELS.get(i))
              .cells(emptyCells())
              .build());
    }

    Map<LocalDate, WeekBoardDayResponse> dayIndex =
        days.stream().collect(Collectors.toMap(WeekBoardDayResponse::getDate, Function.identity()));

    Set<Long> staffIds = new HashSet<>();
    for (Shift shift : shifts) {
      staffIds.add(shift.getStaffId());
      LocalDate boardDate = ShiftSlotMapper.boardDate(shift.getStartAt());
      WeekBoardDayResponse day = dayIndex.get(boardDate);
      if (day == null) {
        continue;
      }
      SlotKind slot = ShiftSlotMapper.fromStartAt(shift.getStartAt());
      Staff staff = staffById.get(shift.getStaffId());
      BoardShiftCardResponse card = toCard(shift, staff, slot);
      day.getCells().get(slot).add(card);
    }

    return WeekBoardResponse.builder()
        .weekStart(weekStart)
        .weekEnd(weekEnd)
        .slots(Arrays.asList(SlotKind.values()))
        .days(days)
        .summary(
            WeekBoardSummaryResponse.builder()
                .totalShifts(shifts.size())
                .staffCount(staffIds.size())
                .build())
        .build();
  }

  private Map<Long, Staff> loadStaff(List<Shift> shifts) {
    Set<Long> ids = shifts.stream().map(Shift::getStaffId).collect(Collectors.toSet());
    if (ids.isEmpty()) {
      return Map.of();
    }
    return staffRepository.findAllById(ids).stream()
        .collect(Collectors.toMap(Staff::getId, Function.identity()));
  }

  private BoardShiftCardResponse toCard(Shift shift, Staff staff, SlotKind slot) {
    return BoardShiftCardResponse.builder()
        .shiftId(shift.getId())
        .staffId(shift.getStaffId())
        .staffName(staff != null ? staff.getName() : "Staff #" + shift.getStaffId())
        .department(staff != null ? staff.getDepartment() : "")
        .role(staff != null ? staff.getRole() : "")
        .startAt(shift.getStartAt())
        .endAt(shift.getEndAt())
        .shiftType(shift.getShiftType())
        .slot(slot)
        .revisionCount(shift.getRevisionCount())
        .warnings(List.of())
        .build();
  }

  private EnumMap<SlotKind, List<BoardShiftCardResponse>> emptyCells() {
    EnumMap<SlotKind, List<BoardShiftCardResponse>> cells = new EnumMap<>(SlotKind.class);
    for (SlotKind slot : SlotKind.values()) {
      cells.put(slot, new ArrayList<>());
    }
    return cells;
  }
}
