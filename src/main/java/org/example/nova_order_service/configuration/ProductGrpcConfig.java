package org.example.nova_order_service.configuration;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.novamarket.grpc.product.ProductServiceGrpc;

@Configuration
public class ProductGrpcConfig {
    @Value("${product.grpc.host}")
    private String productGrpcHost;

    @Value("${product.grpc.port}")
    private int productGrpcPort;

    @Bean
    public ManagedChannel productChannel() {
        return ManagedChannelBuilder.forAddress(productGrpcHost,productGrpcPort)
                .usePlaintext()
                .build();
    }

    @Bean
    public ProductServiceGrpc.ProductServiceBlockingStub productStub(ManagedChannel productChannel){
        return ProductServiceGrpc.newBlockingStub(productChannel);
    }
}
