package com.ecommerce.service;

import com.ecommerce.domain.entity.Product;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Product Service - Handles product operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public Flux<Product> getAllProducts() {
        return productRepository.findAll()
                .doOnComplete(() -> log.info("Retrieved all products"));
    }

    public Mono<Product> getProductById(String id) {
        return productRepository.findById(id)
                .doOnSuccess(p -> {
                    if (p != null) {
                        log.info("Found product: {}", p.getName());
                    } else {
                        log.warn("Product not found with id: {}", id);
                    }
                });
    }

    public Mono<Product> createProduct(Product product) {
        return productRepository.save(product)
                .doOnSuccess(p -> log.info("Created product: {}", p.getName()));
    }

    public Mono<Product> updateProduct(String id, Product product) {
        return productRepository.findById(id)
                .flatMap(existing -> {
                    existing.setName(product.getName());
                    existing.setDescription(product.getDescription());
                    existing.setPrice(product.getPrice());
                    existing.setStockQuantity(product.getStockQuantity());
                    return productRepository.save(existing);
                })
                .doOnSuccess(p -> log.info("Updated product: {}", p.getName()));
    }

    @Transactional
    public Mono<Boolean> reserveStock(String productId, Integer quantity) {
        return productRepository.reserveStock(productId, quantity)
                .map(rowsUpdated -> {
                    boolean success = rowsUpdated > 0;
                    if (success) {
                        log.info("Reserved {} units of product {}", quantity, productId);
                    } else {
                        log.warn("Failed to reserve stock for product {}", productId);
                    }
                    return success;
                });
    }

    @Transactional
    public Mono<Boolean> releaseStock(String productId, Integer quantity) {
        return productRepository.releaseStock(productId, quantity)
                .map(rowsUpdated -> {
                    boolean success = rowsUpdated > 0;
                    if (success) {
                        log.info("Released {} units of product {}", quantity, productId);
                    } else {
                        log.warn("Failed to release stock for product {}", productId);
                    }
                    return success;
                });
    }

    public Mono<Boolean> checkStockAvailability(String productId, Integer quantity) {
        return productRepository.findById(productId)
                .map(product -> product.getStockQuantity() >= quantity)
                .defaultIfEmpty(false);
    }
}
