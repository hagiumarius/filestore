package com.unitedinternet.filestore.config;

import com.unitedinternet.filestore.controllers.FileStoreResponse;
import com.unitedinternet.filestore.exceptions.GenericException;
import com.unitedinternet.filestore.exceptions.RecordNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
Advice used for general exception handling
 */
@RestControllerAdvice
public class AppExceptionHandler
        extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value
            = { jakarta.persistence.EntityNotFoundException.class})
    protected ResponseEntity<Object> handleEntityNotFound(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "Record not found";
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value
            = { com.unitedinternet.filestore.exceptions.ValidationException.class})
    protected ResponseEntity<Object> handleValidationException(
            RuntimeException ex, WebRequest request) {
        FileStoreResponse errorResponse = new FileStoreResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(value
            = { RecordNotFoundException.class})
    protected ResponseEntity<Object> handleRecordNotFoundException(
            RuntimeException ex, WebRequest request) {
        FileStoreResponse errorResponse = new FileStoreResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(value
            = { GenericException.class})
    protected ResponseEntity<Object> handleGenericException(
            RuntimeException ex, WebRequest request) {
        FileStoreResponse errorResponse = new FileStoreResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(value
            = { SQLIntegrityConstraintViolationException.class})
    protected ResponseEntity<Object> handleSQLIntegrityValidationException(
            RuntimeException ex, WebRequest request) {
        FileStoreResponse errorResponse = new FileStoreResponse(HttpStatus.BAD_REQUEST.value(), "Database integrity violation");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @Nullable
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    }