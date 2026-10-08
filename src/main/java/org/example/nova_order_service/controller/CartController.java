package org.example.nova_order_service.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.nova_order_service.dto.CartResponse;
import org.example.nova_order_service.dto.ProductDto;
import org.example.nova_order_service.service.CartService;
import org.example.nova_order_service.validator.AddItemRequest;
import org.example.nova_order_service.validator.UpdateItemRequest;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
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

    @PatchMapping("/items/{productId}")
    public ProductDto updateItemQty(
            @RequestHeader("x-user-id") UUID buyerId,
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateItemRequest qty
            ){
        return service.updateItemQty(buyerId, productId, qty.qty());
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(
            @RequestHeader("x-user-id") UUID buyerId,
            @PathVariable UUID productId
    ){
        service.deleteItemByUserId(buyerId,productId);
        return ResponseEntity.noContent().build();
    }
}
