/* Nhom I */
package com.rotaguard.support;

import com.rotaguard.domain.enums.ShiftType;
import com.rotaguard.web.response.ImportLineError;
import java.io.InputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Value;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

@Value
public class RosterExcelParser {

  public record ParsedStaff(String name, String role, String department) {}

  public record ParsedShift(
      ParsedStaff staff,
      LocalDate date,
      ZonedDateTime startAt,
      ZonedDateTime endAt,
      ShiftType shiftType) {}

  public record ParseResult(
      LocalDate weekStart,
      List<ParsedStaff> staffRows,
      List<ParsedShift> shifts,
      List<ImportLineError> errors) {}

  private static final DataFormatter FORMATTER = new DataFormatter();
  private static final Pattern TIME_RANGE =
      Pattern.compile("^(\\d{1,2})(?::(\\d{2}))?\\s*[-–]\\s*(\\d{1,2})(?::(\\d{2}))?$");

  private static final Map<String, Integer> DAY_COLUMNS =
      Map.ofEntries(
          Map.entry("t2", 0),
          Map.entry("mon", 0),
          Map.entry("monday", 0),
          Map.entry("thu2", 0),
          Map.entry("t3", 1),
          Map.entry("tue", 1),
          Map.entry("tuesday", 1),
          Map.entry("thu3", 1),
          Map.entry("t4", 2),
          Map.entry("wed", 2),
          Map.entry("wednesday", 2),
          Map.entry("thu4", 2),
          Map.entry("t5", 3),
          Map.entry("thu", 3),
          Map.entry("thursday", 3),
          Map.entry("thu5", 3),
          Map.entry("t6", 4),
          Map.entry("fri", 4),
          Map.entry("friday", 4),
          Map.entry("thu6", 4),
          Map.entry("t7", 5),
          Map.entry("sat", 5),
          Map.entry("saturday", 5),
          Map.entry("thu7", 5),
          Map.entry("cn", 6),
          Map.entry("sun", 6),
          Map.entry("sunday", 6),
          Map.entry("chunhat", 6));

  public static ParseResult parse(InputStream input, LocalDate defaultWeekStart) {
    List<ImportLineError> errors = new ArrayList<>();
    List<ParsedStaff> staffRows = new ArrayList<>();
    List<ParsedShift> shifts = new ArrayList<>();
    LocalDate weekStart = defaultWeekStart;

    try (Workbook workbook = WorkbookFactory.create(input)) {
      Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
      if (sheet == null) {
        errors.add(ImportLineError.builder().line(1).message("Excel has no sheets").build());
        return new ParseResult(weekStart, staffRows, shifts, errors);
      }

      weekStart = readWeekStart(sheet, weekStart, errors);
      int headerRowIdx = findHeaderRow(sheet);
      if (headerRowIdx < 0) {
        errors.add(
            ImportLineError.builder()
                .line(1)
                .message("Cannot find header row (name, role, department, ...)")
                .build());
        return new ParseResult(weekStart, staffRows, shifts, errors);
      }

      Row header = sheet.getRow(headerRowIdx);
      Map<String, Integer> cols = mapHeaders(header);
      if (cols.containsKey("date") && cols.containsKey("start")) {
        parseLongFormat(sheet, headerRowIdx, cols, weekStart, staffRows, shifts, errors);
      } else if (hasDayColumns(cols)) {
        parseWideFormat(sheet, headerRowIdx, cols, weekStart, staffRows, shifts, errors);
      } else {
        errors.add(
            ImportLineError.builder()
                .line(headerRowIdx + 1)
                .message("Expected wide format (T2..CN) or long format (date,start,end,shiftType)")
                .build());
      }
    } catch (Exception ex) {
      errors.add(
          ImportLineError.builder()
              .line(1)
              .message("Excel read error: " + ex.getMessage())
              .build());
    }

    return new ParseResult(weekStart, staffRows, shifts, errors);
  }

  private static LocalDate readWeekStart(
      Sheet sheet, LocalDate defaultWeekStart, List<ImportLineError> errors) {
    for (int r = 0; r <= Math.min(3, sheet.getLastRowNum()); r++) {
      Row row = sheet.getRow(r);
      if (row == null) {
        continue;
      }
      for (int c = 0; c < row.getLastCellNum(); c++) {
        String text = cellText(row.getCell(c)).toLowerCase(Locale.ROOT);
        if (text.contains("weekstart") || text.contains("tuan")) {
          String next = cellText(row.getCell(c + 1));
          LocalDate parsed = tryParseDate(next);
          if (parsed != null) {
            return parsed;
          }
        }
        LocalDate parsed = tryParseDate(cellText(row.getCell(c)));
        if (parsed != null && text.isEmpty() && c > 0) {
          String prev = cellText(row.getCell(c - 1)).toLowerCase(Locale.ROOT);
          if (prev.contains("week") || prev.contains("tuan")) {
            return parsed;
          }
        }
      }
    }
    if (defaultWeekStart == null) {
      errors.add(
          ImportLineError.builder()
              .line(1)
              .message("weekStart not found in sheet; pass weekStart query param (Monday)")
              .build());
    }
    return defaultWeekStart;
  }

  private static int findHeaderRow(Sheet sheet) {
    for (int r = 0; r <= sheet.getLastRowNum(); r++) {
      Row row = sheet.getRow(r);
      if (row == null) {
        continue;
      }
      String joined = rowToLower(row);
      if (joined.contains("name")
          || joined.contains("ho ten")
          || joined.contains("họ tên")
          || joined.contains("hoten")) {
        return r;
      }
    }
    return -1;
  }

  private static Map<String, Integer> mapHeaders(Row header) {
    Map<String, Integer> cols = new HashMap<>();
    for (int c = 0; c < header.getLastCellNum(); c++) {
      String key = normalizeHeader(cellText(header.getCell(c)));
      if (!key.isEmpty()) {
        cols.put(key, c);
      }
    }
    return cols;
  }

  private static boolean hasDayColumns(Map<String, Integer> cols) {
    return cols.keySet().stream().anyMatch(DAY_COLUMNS::containsKey);
  }

  private static void parseWideFormat(
      Sheet sheet,
      int headerRowIdx,
      Map<String, Integer> cols,
      LocalDate weekStart,
      List<ParsedStaff> staffRows,
      List<ParsedShift> shifts,
      List<ImportLineError> errors) {
    int nameCol = col(cols, "name", "hoten", "staffname");
    int roleCol = col(cols, "role", "vaitro", "chucdanh");
    int deptCol = col(cols, "department", "khoa", "dept");
    if (nameCol < 0) {
      errors.add(
          ImportLineError.builder().line(headerRowIdx + 1).message("Missing name column").build());
      return;
    }

    for (int r = headerRowIdx + 1; r <= sheet.getLastRowNum(); r++) {
      Row row = sheet.getRow(r);
      if (row == null || isEmptyRow(row)) {
        continue;
      }
      int line = r + 1;
      String name = cellText(row.getCell(nameCol)).trim();
      if (name.isEmpty()) {
        continue;
      }
      String role = roleCol >= 0 ? cellText(row.getCell(roleCol)).trim() : "NURSE";
      String dept = deptCol >= 0 ? cellText(row.getCell(deptCol)).trim() : "ICU";
      if (role.isEmpty()) {
        role = "NURSE";
      }
      if (dept.isEmpty()) {
        dept = "ICU";
      }
      ParsedStaff staff = new ParsedStaff(name, role.toUpperCase(Locale.ROOT), dept);
      staffRows.add(staff);

      for (Map.Entry<String, Integer> entry : cols.entrySet()) {
        Integer dayOffset = DAY_COLUMNS.get(entry.getKey());
        if (dayOffset == null) {
          continue;
        }
        String cell = cellText(row.getCell(entry.getValue())).trim();
        if (cell.isEmpty() || isOff(cell)) {
          continue;
        }
        try {
          LocalDate day = weekStart.plusDays(dayOffset);
          shifts.addAll(parseShiftCell(staff, day, cell));
        } catch (Exception ex) {
          errors.add(
              ImportLineError.builder()
                  .line(line)
                  .message(entry.getKey() + ": " + ex.getMessage())
                  .build());
        }
      }
    }
  }

  private static void parseLongFormat(
      Sheet sheet,
      int headerRowIdx,
      Map<String, Integer> cols,
      LocalDate weekStart,
      List<ParsedStaff> staffRows,
      List<ParsedShift> shifts,
      List<ImportLineError> errors) {
    int nameCol = col(cols, "name", "hoten", "staffname");
    int roleCol = col(cols, "role", "vaitro");
    int deptCol = col(cols, "department", "khoa");
    int dateCol = cols.get("date");
    int startCol = col(cols, "start", "startat", "batdau");
    int endCol = col(cols, "end", "endat", "ketthuc");
    int typeCol = col(cols, "shifttype", "type", "loaica");

    for (int r = headerRowIdx + 1; r <= sheet.getLastRowNum(); r++) {
      Row row = sheet.getRow(r);
      if (row == null || isEmptyRow(row)) {
        continue;
      }
      int line = r + 1;
      try {
        String name = cellText(row.getCell(nameCol)).trim();
        if (name.isEmpty()) {
          continue;
        }
        String role =
            roleCol >= 0 ? cellText(row.getCell(roleCol)).trim().toUpperCase(Locale.ROOT) : "NURSE";
        String dept = deptCol >= 0 ? cellText(row.getCell(deptCol)).trim() : "ICU";
        ParsedStaff staff =
            new ParsedStaff(name, role.isEmpty() ? "NURSE" : role, dept.isEmpty() ? "ICU" : dept);
        staffRows.add(staff);

        LocalDate date = tryParseDate(cellText(row.getCell(dateCol)));
        if (date == null) {
          errors.add(ImportLineError.builder().line(line).message("Invalid date").build());
          continue;
        }
        String startRaw = cellText(row.getCell(startCol));
        String endRaw = cellText(row.getCell(endCol));
        ShiftType type =
            typeCol >= 0
                ? ShiftType.valueOf(cellText(row.getCell(typeCol)).trim().toUpperCase(Locale.ROOT))
                : null;
        ParsedShift shift = buildShift(staff, date, startRaw, endRaw, type);
        shifts.add(shift);
      } catch (Exception ex) {
        errors.add(ImportLineError.builder().line(line).message(ex.getMessage()).build());
      }
    }
  }

  private static List<ParsedShift> parseShiftCell(ParsedStaff staff, LocalDate date, String cell) {
    String normalized = cell.replace(" ", "");
    Matcher m = TIME_RANGE.matcher(normalized);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid shift time: " + cell);
    }
    int sh = Integer.parseInt(m.group(1));
    int sm = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
    int eh = Integer.parseInt(m.group(3));
    int em = m.group(4) != null ? Integer.parseInt(m.group(4)) : 0;
    return List.of(buildShift(staff, date, formatTime(sh, sm), formatTime(eh, em), null));
  }

  private static ParsedShift buildShift(
      ParsedStaff staff, LocalDate date, String startRaw, String endRaw, ShiftType explicitType) {
    LocalTime startTime = parseTime(startRaw);
    LocalTime endTime = parseTime(endRaw);
    LocalDate endDate = date;
    if (endTime.isBefore(startTime) || endTime.equals(LocalTime.MIDNIGHT)) {
      endDate = date.plusDays(1);
    }
    ZonedDateTime startAt = date.atTime(startTime).atZone(AppTimeZones.APP_ZONE);
    ZonedDateTime endAt = endDate.atTime(endTime).atZone(AppTimeZones.APP_ZONE);
    ShiftType type =
        explicitType != null
            ? explicitType
            : inferShiftType(startTime, endTime, endDate.isAfter(date));
    return new ParsedShift(staff, date, startAt, endAt, type);
  }

  private static ShiftType inferShiftType(LocalTime start, LocalTime end, boolean crossesMidnight) {
    if (crossesMidnight || start.getHour() >= 22 || end.getHour() < 8) {
      return ShiftType.NIGHT;
    }
    return ShiftType.DAY;
  }

  private static LocalTime parseTime(String raw) {
    String t = raw.trim();
    if (t.contains("T")) {
      return LocalTime.parse(t.substring(t.indexOf('T') + 1).replace("Z", ""));
    }
    if (t.contains(":")) {
      String[] p = t.split(":");
      return LocalTime.of(Integer.parseInt(p[0]), p.length > 1 ? Integer.parseInt(p[1]) : 0);
    }
    if (t.contains("-")) {
      return parseTime(t.split("-")[0]);
    }
    return LocalTime.of(Integer.parseInt(t), 0);
  }

  private static String formatTime(int h, int m) {
    return String.format(Locale.ROOT, "%02d:%02d", h, m);
  }

  private static int col(Map<String, Integer> cols, String... keys) {
    for (String k : keys) {
      if (cols.containsKey(k)) {
        return cols.get(k);
      }
    }
    return -1;
  }

  private static boolean isOff(String cell) {
    String v = cell.trim().toUpperCase(Locale.ROOT);
    return v.isEmpty() || v.equals("OFF") || v.equals("N") || v.equals("-") || v.equals("X");
  }

  private static LocalDate tryParseDate(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    try {
      if (text.length() >= 10 && text.contains("-")) {
        return LocalDate.parse(text.substring(0, 10));
      }
    } catch (Exception ignored) {
      return null;
    }
    return null;
  }

  private static String cellText(Cell cell) {
    if (cell == null || cell.getCellType() == CellType.BLANK) {
      return "";
    }
    return FORMATTER.formatCellValue(cell).trim();
  }

  private static String normalizeHeader(String value) {
    String ascii = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    return ascii.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
  }

  private static String rowToLower(Row row) {
    StringBuilder sb = new StringBuilder();
    for (int c = 0; c < row.getLastCellNum(); c++) {
      sb.append(normalizeHeader(cellText(row.getCell(c)))).append(' ');
    }
    return sb.toString();
  }

  private static boolean isEmptyRow(Row row) {
    for (int c = 0; c < row.getLastCellNum(); c++) {
      if (!cellText(row.getCell(c)).isEmpty()) {
        return false;
      }
    }
    return true;
  }
}
