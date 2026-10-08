package org.example.nova_order_service.controller;

import jakarta.validation.Valid;
import org.example.nova_order_service.Entities.CartItem;
import org.example.nova_order_service.dto.CartItemResponse;
import org.example.nova_order_service.dto.ProductDto;
import org.example.nova_order_service.service.CartService;
import org.example.nova_order_service.validator.AddItemRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService service;

    public CartController(CartService cartService){
        this.service = cartService;
    }

    @PostMapping("/items")
    public ProductDto addItem(
            @RequestHeader("x-user-id")UUID buyerId,
            @Valid @RequestBody AddItemRequest request
            ){
        return service.addItem(buyerId,request.productId(),request.qty());
    }
}
