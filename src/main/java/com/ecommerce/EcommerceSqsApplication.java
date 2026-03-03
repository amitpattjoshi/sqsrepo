package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * E-commerce SQS Demo Application
 * 
 * This application demonstrates an event-driven architecture using:
 * - Spring WebFlux (reactive programming)
 * - AWS SQS (message queuing)
 * - R2DBC (reactive database access)
 * 
 * Flow:
 * 1. Order Created → Inventory Queue
 * 2. Inventory Consumer → Stock Reserved → Payment Queue
 * 3. Payment Consumer → Payment Completed → Notification Queue
 * 4. Notification Consumer → Send Email/SMS
 */
@SpringBootApplication
public class EcommerceSqsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceSqsApplication.class, args);
    }
}
