/* Nhom I */
package com.rotaguard.support;

import com.rotaguard.domain.enums.ShiftType;
import com.rotaguard.web.request.ShiftImportItemRequest;
import com.rotaguard.web.response.ImportLineError;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ShiftCsvParser {

  private static final List<String> HEADER = List.of("staffid", "startat", "endat", "shifttype");

  public static CsvParseResult<ShiftImportItemRequest> parse(String content) {
    List<ShiftImportItemRequest> rows = new ArrayList<>();
    List<ImportLineError> errors = new ArrayList<>();
    String[] lines = content.split("\\r?\\n");
    if (lines.length == 0 || lines[0].isBlank()) {
      errors.add(ImportLineError.builder().line(1).message("CSV is empty").build());
      return new CsvParseResult<>(rows, errors);
    }

    String[] headerCols = splitLine(lines[0]);
    if (!headerMatches(headerCols)) {
      errors.add(
          ImportLineError.builder()
              .line(1)
              .message("Header must be: staffId,startAt,endAt,shiftType")
              .build());
      return new CsvParseResult<>(rows, errors);
    }

    for (int i = 1; i < lines.length; i++) {
      String line = lines[i].trim();
      if (line.isEmpty()) {
        continue;
      }
      int lineNum = i + 1;
      String[] cols = splitLine(line);
      if (cols.length < 4) {
        errors.add(
            ImportLineError.builder()
                .line(lineNum)
                .message("Expected staffId,startAt,endAt,shiftType")
                .build());
        continue;
      }
      try {
        ShiftImportItemRequest item = new ShiftImportItemRequest();
        item.setStaffId(Long.parseLong(cols[0].trim()));
        item.setStartAt(ZonedDateTime.parse(cols[1].trim()));
        item.setEndAt(ZonedDateTime.parse(cols[2].trim()));
        item.setShiftType(ShiftType.valueOf(cols[3].trim().toUpperCase(Locale.ROOT)));
        if (!item.getEndAt().isAfter(item.getStartAt())) {
          errors.add(
              ImportLineError.builder()
                  .line(lineNum)
                  .message("endAt must be after startAt")
                  .build());
          continue;
        }
        rows.add(item);
      } catch (Exception ex) {
        errors.add(
            ImportLineError.builder()
                .line(lineNum)
                .message("Parse error: " + ex.getMessage())
                .build());
      }
    }
    return new CsvParseResult<>(rows, errors);
  }

  private static boolean headerMatches(String[] cols) {
    if (cols.length < 4) {
      return false;
    }
    for (int i = 0; i < 4; i++) {
      if (!HEADER.get(i).equals(normalizeHeader(cols[i]))) {
        return false;
      }
    }
    return true;
  }

  private static String normalizeHeader(String value) {
    return value.trim().toLowerCase(Locale.ROOT).replace("_", "");
  }

  private static String[] splitLine(String line) {
    return Arrays.stream(line.split(",", -1)).map(String::trim).toArray(String[]::new);
  }
}
