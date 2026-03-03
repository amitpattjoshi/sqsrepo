package com.ecommerce.controller;

import com.ecommerce.domain.entity.Product;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Product Controller - REST API for product management.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Get all products.
     */
    @GetMapping
    public Flux<Product> getAllProducts() {
        log.info("Fetching all products");
        return productService.getAllProducts();
    }

    /**
     * Get product by ID.
     */
    @GetMapping("/{id}")
    public Mono<Product> getProduct(@PathVariable String id) {
        log.info("Fetching product: {}", id);
        return productService.getProductById(id);
    }

    /**
     * Create a new product.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Product> createProduct(@Valid @RequestBody Product product) {
        log.info("Creating product: {}", product.getName());
        return productService.createProduct(product);
    }

    /**
     * Update a product.
     */
    @PutMapping("/{id}")
    public Mono<Product> updateProduct(@PathVariable String id, @Valid @RequestBody Product product) {
        log.info("Updating product: {}", id);
        return productService.updateProduct(id, product);
    }

    /**
     * Check stock availability for a product.
     */
    @GetMapping("/{id}/availability")
    public Mono<StockAvailabilityResponse> checkAvailability(
            @PathVariable String id,
            @RequestParam Integer quantity) {
        log.info("Checking availability for product: {}, quantity: {}", id, quantity);
        return productService.checkStockAvailability(id, quantity)
                .map(available -> new StockAvailabilityResponse(id, quantity, available));
    }

    /**
     * Response DTO for stock availability check.
     */
    public record StockAvailabilityResponse(String productId, Integer requestedQuantity, Boolean available) {
    }
}
