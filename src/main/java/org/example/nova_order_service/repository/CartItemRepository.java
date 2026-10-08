package org.example.nova_order_service.repository;

import org.example.nova_order_service.Entities.Cart;
import org.example.nova_order_service.Entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartIdAndProductId(UUID cartId,UUID productId);

    void deleteByCartAndProductId(Cart cart, UUID productId);
}
