package com.ecommerce.repository;

import com.ecommerce.domain.entity.OrderItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, String> {

    Flux<OrderItem> findByOrderId(String orderId);
}
