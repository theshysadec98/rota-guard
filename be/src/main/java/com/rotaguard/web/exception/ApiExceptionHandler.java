/* Nhom I */
package com.rotaguard.web.exception;

import com.rotaguard.web.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleNotFound(NoSuchElementException ex) {
    return ErrorResponse.builder().error(ex.getMessage()).build();
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                    (a, b) -> a));
    return ErrorResponse.builder().error("Validation failed").fieldErrors(fieldErrors).build();
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleBadRequest(IllegalArgumentException ex) {
    return ErrorResponse.builder().error(ex.getMessage()).build();
  }

  @ExceptionHandler({
    MissingServletRequestParameterException.class,
    MethodArgumentTypeMismatchException.class,
    ConstraintViolationException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleParameterErrors(Exception ex) {
    return ErrorResponse.builder().error(ex.getMessage()).build();
  }

  @ExceptionHandler(ImportException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleImport(ImportException ex) {
    return ErrorResponse.builder().error(ex.getMessage()).importErrors(ex.getErrors()).build();
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleGeneric(Exception ex) {
    return ErrorResponse.builder()
        .error(ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName())
        .build();
  }
}
