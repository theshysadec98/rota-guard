/* Nhom I */
package com.rotaguard.support;

import com.rotaguard.web.response.ImportLineError;
import java.util.List;
import lombok.Value;

@Value
public class CsvParseResult<T> {
  List<T> rows;
  List<ImportLineError> errors;

  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }
}
