/* Nhom I */
package com.rotaguard.support;

import com.rotaguard.web.request.StaffImportItemRequest;
import com.rotaguard.web.response.ImportLineError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StaffCsvParser {

  private static final List<String> HEADER =
      List.of("name", "role", "department", "sensitivityfactor");

  public static CsvParseResult<StaffImportItemRequest> parse(String content) {
    List<StaffImportItemRequest> rows = new ArrayList<>();
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
              .message("Header must be: name,role,department,sensitivityFactor")
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
      if (cols.length < 3) {
        errors.add(
            ImportLineError.builder()
                .line(lineNum)
                .message("Expected at least name,role,department")
                .build());
        continue;
      }
      try {
        StaffImportItemRequest item = new StaffImportItemRequest();
        item.setName(cols[0].trim());
        item.setRole(cols[1].trim().toUpperCase(Locale.ROOT));
        item.setDepartment(cols[2].trim());
        item.setSensitivityFactor(cols.length > 3 ? Double.parseDouble(cols[3].trim()) : 1.0);
        if (item.getName().isEmpty()
            || item.getRole().isEmpty()
            || item.getDepartment().isEmpty()) {
          errors.add(
              ImportLineError.builder()
                  .line(lineNum)
                  .message("name, role, department required")
                  .build());
          continue;
        }
        rows.add(item);
      } catch (NumberFormatException ex) {
        errors.add(
            ImportLineError.builder()
                .line(lineNum)
                .message("Invalid sensitivityFactor: " + cols[3])
                .build());
      }
    }
    return new CsvParseResult<>(rows, errors);
  }

  private static boolean headerMatches(String[] cols) {
    if (cols.length < 3) {
      return false;
    }
    for (int i = 0; i < 3; i++) {
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
