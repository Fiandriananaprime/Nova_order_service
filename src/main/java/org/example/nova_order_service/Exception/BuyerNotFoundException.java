package org.example.nova_order_service.Exception;

public class BuyerNotFoundException extends RuntimeException{
    public BuyerNotFoundException(String message){
        super(message);
    }
}
