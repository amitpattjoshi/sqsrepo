package com.ecommerce.repository;

import com.ecommerce.domain.entity.Product;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, String> {

    @Modifying
    @Query("UPDATE products SET stock_quantity = stock_quantity - :quantity WHERE id = :productId AND stock_quantity >= :quantity")
    Mono<Integer> reserveStock(String productId, Integer quantity);

    @Modifying
    @Query("UPDATE products SET stock_quantity = stock_quantity + :quantity WHERE id = :productId")
    Mono<Integer> releaseStock(String productId, Integer quantity);
}
