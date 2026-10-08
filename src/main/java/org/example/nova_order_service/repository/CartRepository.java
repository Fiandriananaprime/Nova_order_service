package org.example.nova_order_service.repository;

import org.example.nova_order_service.Entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findCartByBuyerId(UUID buyerId);
}
