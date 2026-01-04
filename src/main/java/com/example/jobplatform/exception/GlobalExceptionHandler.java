package com.example.jobplatform.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
         Map<String, String> error = new HashMap<>();
         error.put("error", ex.getMessage());
         
         if (ex.getMessage() != null && ex.getMessage().contains("Job not found")) {
             return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
         }
         return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


