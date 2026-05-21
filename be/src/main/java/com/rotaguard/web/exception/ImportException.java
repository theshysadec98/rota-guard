/* Nhom I */
package com.rotaguard.web.exception;

import com.rotaguard.web.response.ImportLineError;
import java.util.List;
import lombok.Getter;

@Getter
public class ImportException extends RuntimeException {

  private final List<ImportLineError> errors;

  public ImportException(String message, List<ImportLineError> errors) {
    super(message);
    this.errors = errors;
  }
}
