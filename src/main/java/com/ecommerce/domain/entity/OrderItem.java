package com.ecommerce.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("order_items")
public class OrderItem {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String orderId;
    private String productId;
    private Integer quantity;
    private BigDecimal unitPrice;

    @Version
    private Long version;
}
