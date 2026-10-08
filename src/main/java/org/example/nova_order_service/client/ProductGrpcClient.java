package org.example.nova_order_service.client;

import com.novamarket.grpc.product.*;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductGrpcClient {
    private final ProductServiceGrpc.ProductServiceBlockingStub stub;


    public ProductGrpcClient(ProductServiceGrpc.ProductServiceBlockingStub stub){
        this.stub = stub;
    }

    public List<Product> getProducts(List<UUID> productIds) {
        GetProductsRequest request =
                GetProductsRequest.newBuilder()
                        .addAllProductIds(
                                productIds.stream()
                                        .map(UUID::toString)
                                        .toList()
                        )
                        .build();

        GetProductsResponse response = stub.getProducts(request);

        return response.getProductsList();
    }

    public Product getProduct(UUID productId) {
        GetProductRequest request = GetProductRequest.newBuilder()
                .setProductId(productId.toString())
                .build();
        return stub.getProduct(request);
    }
}
