package com.ecommerce.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("products")
public class Product {

    @Id
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private LocalDateTime createdAt;
}
