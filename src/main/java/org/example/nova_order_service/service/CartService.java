package org.example.nova_order_service.service;

import com.novamarket.grpc.product.Product;
import lombok.AllArgsConstructor;
import org.example.nova_order_service.Entities.Cart;
import org.example.nova_order_service.Entities.CartItem;
import org.example.nova_order_service.Exception.InsufficientStockException;
import org.example.nova_order_service.client.ProductGrpcClient;
import org.example.nova_order_service.dto.ProductDto;
import org.example.nova_order_service.event.ItemAdded;
import org.example.nova_order_service.repository.CartItemRepository;
import org.example.nova_order_service.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CartService {
    private final  CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final amqpPublisherService publisherService;
    private final ProductGrpcClient productClient;

    @Transactional
    public ProductDto addItem (UUID buyerId, UUID productId, int quantity){

        Cart cart = cartRepository.findCartByBuyerId(buyerId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setBuyerId(buyerId);
            return cartRepository.save(newCart);
        });

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        Product product = productClient.getProduct(productId);

        CartItem item;
        if(existingItem.isPresent()){
            item = existingItem.get();
            item.setQty(item.getQty() + quantity);
            if(product.getStock()< item.getQty()) throw new InsufficientStockException("Insufficient stock");
        }
        else {
            if(product.getStock()<quantity) throw new InsufficientStockException("Insufficient stock");
            item = new CartItem();
            item.setCart(cart);
            item.setQty(quantity);
            item.setProductId(productId);
            publisherService.Publish(new ItemAdded(buyerId, productId, quantity));
        }
        cartRepository.save(cart);
        cartItemRepository.save(item);

        return new ProductDto(
                UUID.fromString(product.getProductId()),
                product.getName(),
                product.getImage(),
                BigDecimal.valueOf(product.getPrice()),
                item.getQty(),
                UUID.fromString(product.getSellerId()),
                product.getSellerName(),
                product.getStock(),
                product.hasOriginalPrice() ? BigDecimal.valueOf(product.getOriginalPrice()) : null,
                product.hasDiscount() ? BigDecimal.valueOf(product.getDiscount()) : null,
                BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(item.getQty()))
        );
    }
}
