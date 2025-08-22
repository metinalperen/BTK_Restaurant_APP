package com.example.demo.exception;

import com.example.demo.exception.diningtable.*;
import com.example.demo.exception.product.InvalidProductDataException;
import com.example.demo.exception.product.ProductAlreadyExistsException;
import com.example.demo.exception.product.ProductNotActiveException;
import com.example.demo.exception.product.ProductNotFoundException;
import com.example.demo.exception.product.ProductReferencedInOrder;
import com.example.demo.exception.reservation.*;
import com.example.demo.exception.stock.StockConflictException;
import com.example.demo.exception.stock.StockException;
import com.example.demo.exception.stock.StockNotFoundException;
import com.example.demo.exception.stock.StockValidationException;
import com.example.demo.exception.stockmovement.StockMovementConflictException;
import com.example.demo.exception.stockmovement.StockMovementException;
import com.example.demo.exception.stockmovement.StockMovementNotFoundException;
import com.example.demo.exception.stockmovement.StockMovementValidationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ==================== JSON SERIALIZATION/DESERIALIZATION ERRORS ====================
    
    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<Map<String, Object>> handleJsonParseException(JsonParseException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "JSON Parse Error", 
            "JSON formatı geçersiz: " + ex.getMessage());
    }
    
    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<Map<String, Object>> handleJsonMappingException(JsonMappingException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "JSON Mapping Error", 
            "JSON verisi eşleştirilemedi: " + ex.getMessage());
    }
    
    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFormatException(InvalidFormatException ex) {
        String fieldName = ex.getPathReference();
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";
        String targetType = ex.getTargetType() != null ? ex.getTargetType().getSimpleName() : "unknown";
        
        String message = String.format("Alan '%s' için geçersiz format: '%s'. Beklenen tip: %s", 
            fieldName, value, targetType);
        
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Format Error", message);
    }
    
    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ResponseEntity<Map<String, Object>> handleUnrecognizedPropertyException(UnrecognizedPropertyException ex) {
        String propertyName = ex.getPropertyName();
        String message = String.format("Tanınmayan alan: '%s'. Bu alan desteklenmiyor.", propertyName);
        
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Unrecognized Property Error", message);
    }
    
    // ==================== DATE/TIME PARSING ERRORS ====================
    
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Map<String, Object>> handleDateTimeParseException(DateTimeParseException ex) {
        String message = String.format("Tarih/saat formatı geçersiz: '%s'. Hata: %s", 
            ex.getParsedString(), ex.getMessage());
        
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "DateTime Parse Error", message);
    }
    
    // ==================== METHOD ARGUMENT TYPE MISMATCH ====================
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String parameterName = ex.getName();
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        
        String message = String.format("Parametre '%s' için tip uyumsuzluğu: '%s' değeri %s tipine dönüştürülemedi", 
            parameterName, value, requiredType);
        
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Type Mismatch Error", message);
    }

    // Validation hataları için detaylı cevap
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().format(FORMATTER));
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("details", validationErrors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // IllegalArgumentException için
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Illegal Argument", ex.getMessage());
    }

    // ReservationException ailesi için özel handlerlar
    @ExceptionHandler(ReservationValidationException.class)
    public ResponseEntity<Map<String, Object>> handleReservationValidation(ReservationValidationException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Reservation Validation Error", ex.getMessage());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleReservationNotFound(ReservationNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Reservation Not Found", ex.getMessage());
    }

    @ExceptionHandler(ReservationConflictException.class)
    public ResponseEntity<Map<String, Object>> handleReservationConflict(ReservationConflictException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Reservation Conflict", ex.getMessage());
    }

    // ReservationException için (genel)
    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<Map<String, Object>> handleReservationException(ReservationException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Reservation Error", ex.getMessage());
    }

    // ==================== DINING TABLE EXCEPTIONS ====================

    // TableNotFoundException için (404 - Not Found)
    @ExceptionHandler(TableNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTableNotFoundException(TableNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Table Not Found", ex.getMessage());
    }

    // TableNumberException için (409 - Conflict)
    @ExceptionHandler(TableNumberException.class)
    public ResponseEntity<Map<String, Object>> handleTableNumberException(TableNumberException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Table Number Conflict", ex.getMessage());
    }

    // TableCapacityException için (400 - Bad Request)
    @ExceptionHandler(TableCapacityException.class)
    public ResponseEntity<Map<String, Object>> handleTableCapacityException(TableCapacityException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Table Capacity Error", ex.getMessage());
    }

    // TableStatusException için (400 - Bad Request)
    @ExceptionHandler(TableStatusException.class)
    public ResponseEntity<Map<String, Object>> handleTableStatusException(TableStatusException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Table Status Error", ex.getMessage());
    }

    // TableDeletionException için (409 - Conflict)
    @ExceptionHandler(TableDeletionException.class)
    public ResponseEntity<Map<String, Object>> handleTableDeletionException(TableDeletionException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Table Deletion Conflict", ex.getMessage());
    }

    // TableSalonException için (400 - Bad Request)
    @ExceptionHandler(TableSalonException.class)
    public ResponseEntity<Map<String, Object>> handleTableSalonException(TableSalonException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Table Salon Error", ex.getMessage());
    }

    // TableOrderConflictException için (409 - Conflict)
    @ExceptionHandler(TableOrderConflictException.class)
    public ResponseEntity<Map<String, Object>> handleTableOrderConflictException(TableOrderConflictException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Table Order Conflict", ex.getMessage());
    }

    // TableReservationConflictException için (409 - Conflict)
    @ExceptionHandler(TableReservationConflictException.class)
    public ResponseEntity<Map<String, Object>> handleTableReservationConflictException(TableReservationConflictException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Table Reservation Conflict", ex.getMessage());
    }

    // DiningTableException için (genel - 400 - Bad Request)
    @ExceptionHandler(DiningTableException.class)
    public ResponseEntity<Map<String, Object>> handleDiningTableException(DiningTableException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Dining Table Error", ex.getMessage());
    }

    // ==================== OTHER EXCEPTIONS ====================

    // ProductNotFoundException için
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotFound(ProductNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Product Not Found", ex.getMessage());
    }

    // ProductReferencedInOrder için
    @ExceptionHandler(ProductReferencedInOrder.class)
    public ResponseEntity<Map<String, Object>> handleProductReferencedInOrder(ProductReferencedInOrder ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Product Referenced In Order", ex.getMessage());
    }

    // InvalidProductDataException için
    @ExceptionHandler(InvalidProductDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidProductData(InvalidProductDataException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Product Data", ex.getMessage());
    }

    // ProductAlreadyExistsException için
    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleProductAlreadyExists(ProductAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Product Already Exists", ex.getMessage());
    }

    // ProductNotActiveException için
    @ExceptionHandler(ProductNotActiveException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotActive(ProductNotActiveException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Product Not Active", ex.getMessage());
    }

    // ==================== STOCK EXCEPTIONS ====================

    @ExceptionHandler(StockValidationException.class)
    public ResponseEntity<Map<String, Object>> handleStockValidation(StockValidationException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Stock Validation Error", ex.getMessage());
    }

    @ExceptionHandler(StockNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleStockNotFound(StockNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Stock Not Found", ex.getMessage());
    }

    @ExceptionHandler(StockConflictException.class)
    public ResponseEntity<Map<String, Object>> handleStockConflict(StockConflictException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Stock Conflict", ex.getMessage());
    }

    @ExceptionHandler(StockException.class)
    public ResponseEntity<Map<String, Object>> handleStockException(StockException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Stock Error", ex.getMessage());
    }

    // ==================== STOCK MOVEMENT EXCEPTIONS ====================

    @ExceptionHandler(StockMovementValidationException.class)
    public ResponseEntity<Map<String, Object>> handleStockMovementValidation(StockMovementValidationException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Stock Movement Validation Error", ex.getMessage());
    }

    @ExceptionHandler(StockMovementNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleStockMovementNotFound(StockMovementNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Stock Movement Not Found", ex.getMessage());
    }

    @ExceptionHandler(StockMovementConflictException.class)
    public ResponseEntity<Map<String, Object>> handleStockMovementConflict(StockMovementConflictException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Stock Movement Conflict", ex.getMessage());
    }

    @ExceptionHandler(StockMovementException.class)
    public ResponseEntity<Map<String, Object>> handleStockMovementException(StockMovementException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Stock Movement Error", ex.getMessage());
    }

    // ==================== USER EXCEPTIONS ====================
    @ExceptionHandler(com.example.demo.exception.user.UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(com.example.demo.exception.user.UserNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage());
    }

    @ExceptionHandler(com.example.demo.exception.user.UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(com.example.demo.exception.user.UserAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "User Already Exists", ex.getMessage());
    }

    @ExceptionHandler(com.example.demo.exception.user.InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(com.example.demo.exception.user.InvalidCredentialsException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid Credentials", ex.getMessage());
    }

    @ExceptionHandler(com.example.demo.exception.user.UserInactiveException.class)
    public ResponseEntity<Map<String, Object>> handleUserInactive(com.example.demo.exception.user.UserInactiveException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "User Inactive", ex.getMessage());
    }

    @ExceptionHandler(com.example.demo.exception.user.UserPhotoNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserPhotoNotFound(com.example.demo.exception.user.UserPhotoNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "User Photo Not Found", ex.getMessage());
    }

    @ExceptionHandler(com.example.demo.exception.user.PasswordResetTokenInvalidException.class)
    public ResponseEntity<Map<String, Object>> handlePasswordResetTokenInvalid(com.example.demo.exception.user.PasswordResetTokenInvalidException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Password Reset Token Invalid", ex.getMessage());
    }

    @ExceptionHandler(com.example.demo.exception.user.UserException.class)
    public ResponseEntity<Map<String, Object>> handleUserException(com.example.demo.exception.user.UserException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "User Error", ex.getMessage());
    }

    // Diğer tüm istisnalar için genel handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Beklenmedik bir hata oluştu. Lütfen daha sonra tekrar deneyiniz. Hata: " + ex.getMessage());
    }

    // Ortak JSON error response builder
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().format(FORMATTER));
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}
