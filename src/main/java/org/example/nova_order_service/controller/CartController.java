package org.example.nova_order_service.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.nova_order_service.dto.CartResponse;
import org.example.nova_order_service.dto.ProductDto;
import org.example.nova_order_service.service.CartService;
import org.example.nova_order_service.validator.AddItemRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService service;

    @GetMapping("/")
    public CartResponse getCart(
            @RequestHeader("x-user-id") UUID buyerId
    ){
        return service.getCartByBuyerId(buyerId);
    }

    @PostMapping("/items")
    public ProductDto addItem(
            @RequestHeader("x-user-id")UUID buyerId,
            @Valid @RequestBody AddItemRequest request
            ){
        return service.addItem(buyerId,request.productId(),request.qty());
    }

    @DeleteMapping("/")
    public ResponseEntity<Void> deleteCart(
            @RequestHeader("x-user-id") UUID buyerId
    ){
        service.deleteByUserId(buyerId);
        return ResponseEntity.noContent().build();
    }
}
