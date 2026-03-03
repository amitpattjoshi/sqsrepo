package com.ecommerce.repository;

import com.ecommerce.domain.entity.Order;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, String> {

    Flux<Order> findByCustomerId(String customerId);

    @Modifying
    @Query("UPDATE orders SET status = :status, updated_at = CURRENT_TIMESTAMP WHERE id = :orderId")
    Mono<Integer> updateStatus(String orderId, String status);
}
