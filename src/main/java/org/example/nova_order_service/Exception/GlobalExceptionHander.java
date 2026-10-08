package org.example.nova_order_service.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHander {

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<String> handlerInsuffucientStock(InsufficientStockException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(BuyerNotFoundException.class)
    public ResponseEntity<String> handlerUserNotFound(BuyerNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
