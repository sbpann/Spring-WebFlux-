package io.argonlab.fina.exception;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(NotFoundException.class)
  ResponseEntity<?> notFound(NotFoundException ex) {
    return ResponseEntity.status(404).body(Map.of("message", "not found."));
  }
}
