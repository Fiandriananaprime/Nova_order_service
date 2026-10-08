package org.example.nova_order_service.Exception;

public class InsufficientStockException extends RuntimeException{
    public InsufficientStockException (String message){
        super(message);
    }
}
