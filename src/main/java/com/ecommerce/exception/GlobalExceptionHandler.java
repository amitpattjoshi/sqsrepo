package com.ecommerce.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for the E-commerce application.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors.
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(WebExchangeBindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation error: {}", errors);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Invalid request data")
                .details(errors)
                .build();

        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    /**
     * Handle product not found.
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleProductNotFound(ProductNotFoundException ex) {
        log.warn("Product not found: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
    }

    /**
     * Handle order not found.
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleOrderNotFound(OrderNotFoundException ex) {
        log.warn("Order not found: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
    }

    /**
     * Handle insufficient stock.
     */
    @ExceptionHandler(InsufficientStockException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInsufficientStock(InsufficientStockException ex) {
        log.warn("Insufficient stock: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Insufficient Stock")
                .message(ex.getMessage())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(response));
    }

    /**
     * Handle generic exceptions.
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }

    /**
     * Error response DTO.
     */
    @lombok.Builder
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private Map<String, String> details;
    }

    // Custom exceptions
    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(Long id) {
            super("Product not found with id: " + id);
        }
    }

    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String orderId) {
            super("Order not found with id: " + orderId);
        }
    }

    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(Long productId, Integer requested, Integer available) {
            super(String.format("Insufficient stock for product %d: requested %d, available %d",
                    productId, requested, available));
        }
    }
}
