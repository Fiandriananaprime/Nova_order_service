package org.example.nova_order_service.service;

import com.novamarket.grpc.product.Product;
import lombok.AllArgsConstructor;
import org.example.nova_order_service.Entities.Cart;
import org.example.nova_order_service.Entities.CartItem;
import org.example.nova_order_service.Exception.BuyerNotFoundException;
import org.example.nova_order_service.Exception.InsufficientStockException;
import org.example.nova_order_service.Exception.ProductNotFoundException;
import org.example.nova_order_service.client.ProductGrpcClient;
import org.example.nova_order_service.dto.CartResponse;
import org.example.nova_order_service.dto.ProductDto;
import org.example.nova_order_service.event.ItemAdded;
import org.example.nova_order_service.repository.CartItemRepository;
import org.example.nova_order_service.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CartService {
    private final  CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final amqpPublisherService publisherService;
    private final ProductGrpcClient productClient;

    @Transactional(readOnly = true)
    public CartResponse getCartByBuyerId(UUID buyerId){
        Optional<Cart> cart = cartRepository.findCartByBuyerId(buyerId);
        if (cart.isEmpty()) return new CartResponse(List.of(), BigDecimal.ZERO);

        List<UUID> productsIds = cart.get().getItems().stream().map(CartItem::getProductId).toList();
        if (productsIds.isEmpty()) return new CartResponse(List.of(), BigDecimal.ZERO);

        List<Product> productsDetail = productClient.getProducts(productsIds);

        Map<UUID, Product> productsById = productsDetail.stream()
                .collect(Collectors.toMap(
                        p -> UUID.fromString(p.getProductId()),
                        product -> product
                ));

        List<ProductDto> items = cart.get().getItems()
                .stream()
                .map(item -> {
                    Product product = productsById.get(item.getProductId());

                    BigDecimal subtotal = BigDecimal.valueOf(product.getPrice())
                            .multiply(BigDecimal.valueOf(item.getQty()));

                    return new ProductDto(
                            UUID.fromString(product.getProductId()),
                            product.getName(),
                            product.getImage(),
                            BigDecimal.valueOf(product.getPrice()),
                            item.getQty(),
                            UUID.fromString(product.getSellerId()),
                            product.getSellerName(),
                            product.getStock(),
                            BigDecimal.valueOf(product.getOriginalPrice()),
                            BigDecimal.valueOf(product.getDiscount()),
                            subtotal
                    );
                })
                .toList();

        BigDecimal subtotal = items.stream()
                .map(ProductDto::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                items,
                subtotal);
    }

    @Transactional
    public void deleteByUserId(UUID buyerId){
        cartRepository.findCartByBuyerId(buyerId).ifPresent(cartRepository::delete);
    }

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
            cart.getItems().add(item);
            publisherService.Publish(new ItemAdded(buyerId, productId, quantity));
        }
        cart.setUpdatedAt(OffsetDateTime.now());


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

    @Transactional
    public ProductDto updateItemQty(UUID buyerId, UUID productId, int quantity){
        Cart cart = cartRepository.findCartByBuyerId(buyerId).orElseThrow(() -> new BuyerNotFoundException("User Not Found"));
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(),productId)
                .orElseThrow(ProductNotFoundException::new);
        item.setQty(quantity);

        cart.setUpdatedAt(OffsetDateTime.now());
        Product product = productClient.getProduct(productId);

        if(product.getStock()<item.getQty()) throw new InsufficientStockException("Insufficient product stock");

        return new ProductDto(
                UUID.fromString(product.getProductId()),
                product.getName(),
                product.getImage(),
                BigDecimal.valueOf(product.getPrice()),
                quantity,
                UUID.fromString(product.getSellerId()),
                product.getSellerName(),
                product.getStock(),
                product.hasOriginalPrice() ? BigDecimal.valueOf(product.getOriginalPrice()) : null,
                product.hasDiscount() ? BigDecimal.valueOf(product.getDiscount()) : null,
                BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(quantity))
        );
    }

    @Transactional
    public void deleteItemByUserId(UUID buyerId, UUID productId){
        Cart cart = cartRepository.findCartByBuyerId(buyerId).orElseThrow(() -> new BuyerNotFoundException("Buyer Cart not Found"));
        cartItemRepository.deleteByCartAndProductId(cart, productId);
        cart.setUpdatedAt(OffsetDateTime.now());
    }
}
