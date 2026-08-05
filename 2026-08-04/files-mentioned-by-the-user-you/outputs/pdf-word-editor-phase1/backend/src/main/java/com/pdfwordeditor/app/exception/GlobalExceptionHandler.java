package com.pdfwordeditor.app.exception;

import com.pdfwordeditor.app.export.DocumentExportException;
import com.pdfwordeditor.app.parsing.DocumentParseException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
    return error(HttpStatus.BAD_REQUEST, "Validation failed");
  }

  @ExceptionHandler(DocumentParseException.class)
  public ResponseEntity<Map<String, Object>> parseFailed(DocumentParseException ex) {
    return error(HttpStatus.BAD_REQUEST, "Failed to parse uploaded document");
  }

  @ExceptionHandler(DocumentExportException.class)
  public ResponseEntity<Map<String, Object>> exportFailed(DocumentExportException ex) {
    return error(HttpStatus.INTERNAL_SERVER_ERROR, "PDF export failed");
  }

  private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(
        Map.of("status", status.value(), "message", message, "timestamp", Instant.now()));
  }
}
