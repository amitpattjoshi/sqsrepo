# E-commerce Application with Spring WebFlux & AWS SQS

# Quick commands

- aws ssm start-session --target i-0d41c2277b9304ea1 --profile sqs --region eu-west-1
- aws ssm start-session --target $(terraform output --raw ec2_instance_id) --profile sqs --region eu-west-1

/\*\*

# install kubectl

cd /tmp

# Download kubectl

curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"

# Install it

chmod +x kubectl
sudo mv kubectl /usr/local/bin/

# Verify

kubectl version --client

# EKSCTL install

ARCH=amd64

# 2. Set Platform (e.g., Linux_amd64 or Darwin_amd64)

PLATFORM=$(uname -s)_$ARCH

# 3. Now run your curl command (Note: most Linux releases use .tar.gz, not .zip)

curl -sLO "https://github.com/eksctl-io/eksctl/releases/latest/download/eksctl_$PLATFORM.tar.gz"

# 4. Extract and Install

tar -xzf eksctl*$PLATFORM.tar.gz -C /tmp && rm eksctl*$PLATFORM.tar.gz
sudo mv /tmp/eksctl /usr/local/bin

\*\*/

# Create eks cluster

- eksctl create cluster --name sqsecommerce --nodes-min=3 --node-type=t3.medium

# install te ebs driver and attach iam policy to access ebs

eksctl utils associate-iam-oidc-provider --region=eu-west-1 --cluster=sqsecommerce --approve

eksctl create iamserviceaccount --name ebs-csi-controller-sa --namespace kube-system --cluster sqsecommerce --attach-policy-arn arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy --approve --role-only --role-name AmazonEKS_EBS_CSI_DriverRole

eksctl create addon --name aws-ebs-csi-driver --cluster sqsecommerce --service-account-role-arn arn:aws:iam::$(aws sts get-caller-identity --query Account --output text):role/AmazonEKS_EBS_CSI_DriverRole --force

## 📋 Overview

This is an event-driven e-commerce application demonstrating modern reactive architecture using:

- **Spring WebFlux** - Non-blocking reactive web framework
- **AWS SQS** - Message queuing for service decoupling
- **R2DBC** - Reactive database access
- **SAGA Pattern** - Distributed transaction management

## 🏗️ Architecture

```
┌─────────────────┐
│   Client/API    │
│   (REST)        │
└────────┬────────┘
         │ POST /api/orders
         ▼
┌─────────────────┐
│  Order Service  │ ──────────────────────────────────────┐
│  (Create Order) │                                        │
└────────┬────────┘                                        │
         │ OrderCreatedEvent                               │
         ▼                                                 │
┌─────────────────┐                                        │
│  Inventory      │ ◄─── SQS: inventory-queue.fifo        │
│  Queue/Consumer │                                        │
└────────┬────────┘                                        │
         │ Reserve Stock                                   │
         │                                                 │
         ├─── Success: StockReservedEvent                 │
         │              ▼                                  │
         │    ┌─────────────────┐                         │
         │    │  Payment        │ ◄── SQS: payment-queue.fifo
         │    │  Queue/Consumer │                         │
         │    └────────┬────────┘                         │
         │             │                                   │
         │             ├─── Success: PaymentCompletedEvent │
         │             │              ▼                    │
         │             │    ┌─────────────────┐           │
         │             │    │  Notification   │ ◄── SQS: notification-queue.fifo
         │             │    │  Queue/Consumer │           │
         │             │    └────────┬────────┘           │
         │             │             │                     │
         │             │             └─── Send Email 📧    │
         │             │                                   │
         │             └─── Failure: NotificationEvent     │
         │                           (Payment Failed)      │
         │                                                 │
         └─── Failure: NotificationEvent                  │
                       (Insufficient Stock)                │
                       ▼                                   │
              ┌─────────────────┐                         │
              │  Notification   │ ◄────────────────────────┘
              │  Queue/Consumer │
              └─────────────────┘
```

## 📁 Project Structure

```
src/main/java/com/ecommerce/
├── EcommerceSqsApplication.java      # Main application
├── config/
│   ├── SqsConfig.java                # AWS SQS configuration
│   └── DatabaseConfig.java           # R2DBC configuration
├── controller/
│   ├── OrderController.java          # Order REST API
│   └── ProductController.java        # Product REST API
├── service/
│   ├── OrderService.java             # Order business logic
│   ├── ProductService.java           # Product business logic
│   ├── InventoryService.java         # Stock management
│   ├── PaymentService.java           # Payment processing
│   └── NotificationService.java      # Notification handling
├── messaging/
│   ├── publisher/
│   │   └── EventPublisher.java       # Publishes events to SQS
│   └── consumer/
│       ├── InventoryConsumer.java    # Handles inventory events
│       ├── PaymentConsumer.java      # Handles payment events
│       └── NotificationConsumer.java # Handles notifications
├── domain/
│   ├── entity/
│   │   ├── Product.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   └── OrderStatus.java
│   ├── dto/
│   │   ├── CreateOrderRequest.java
│   │   └── OrderResponse.java
│   └── event/
│       ├── OrderCreatedEvent.java
│       ├── StockReservedEvent.java
│       ├── PaymentCompletedEvent.java
│       └── NotificationEvent.java
├── repository/
│   ├── ProductRepository.java
│   ├── OrderRepository.java
│   └── OrderItemRepository.java
└── exception/
    └── GlobalExceptionHandler.java
```

## 🚀 Prerequisites

1. **Java 21** or higher
2. **Maven 3.8+**
3. **AWS Account** with SQS access
4. **AWS CLI** configured with credentials

## 🔧 AWS Setup

### Create FIFO Queues

```bash
# Set AWS profile
export AWS_PROFILE=sqs

# Create Inventory Queue
aws sqs create-queue \
  --queue-name order-inventory-queue.fifo \
  --attributes FifoQueue=true,ContentBasedDeduplication=false

# Create Payment Queue
aws sqs create-queue \
  --queue-name order-payment-queue.fifo \
  --attributes FifoQueue=true,ContentBasedDeduplication=false

# Create Notification Queue
aws sqs create-queue \
  --queue-name order-notification-queue.fifo \
  --attributes FifoQueue=true,ContentBasedDeduplication=false
```

### Verify Queues

```bash
aws sqs list-queues
```

## ⚙️ Configuration

Update `src/main/resources/application.yml` with your AWS settings:

```yaml
spring:
  cloud:
    aws:
      region:
        static: eu-west-1
      credentials:
        profile:
          name: sqs

app:
  sqs:
    queues:
      order: order-queue.fifo
      inventory: order-inventory-queue.fifo
      payment: order-payment-queue.fifo
      notification: order-notification-queue.fifo
```

## 🏃 Running the Application

```bash
# Build the project
./mvnw clean package -DskipTests

# Run the application
./mvnw spring-boot:run

# Or run the JAR directly
java -jar target/ecommerce-sqs-0.0.1-SNAPSHOT.jar
```

The application runs on **port 8082**.

## 🧪 Testing the API

### Get All Products

```bash
curl http://localhost:8082/api/products
```

### Create an Order

```bash
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "customerEmail": "customer@example.com",
    "items": [
      {"productId": 1, "quantity": 2},
      {"productId": 2, "quantity": 1}
    ]
  }'
```

### Get Order Status

```bash
curl http://localhost:8082/api/orders/{orderId}
```

### Get Customer Orders

```bash
curl http://localhost:8082/api/orders/customer/CUST-001
```

## 📊 Order Status Flow

| Status               | Description                                |
| -------------------- | ------------------------------------------ |
| `PENDING`            | Order created, waiting for inventory check |
| `INVENTORY_RESERVED` | Stock reserved, waiting for payment        |
| `PAYMENT_COMPLETED`  | Payment successful, order confirmed        |
| `COMPLETED`          | Order fully processed, notification sent   |
| `FAILED`             | Inventory or order processing failed       |
| `PAYMENT_FAILED`     | Payment declined                           |
| `CANCELLED`          | Order cancelled by customer                |

## 🔍 Event Flow Example

1. **Customer creates order** → `OrderController.createOrder()`
2. **OrderCreatedEvent** published to `inventory-queue`
3. **InventoryConsumer** receives event, reserves stock
4. If stock OK → **StockReservedEvent** to `payment-queue`
5. **PaymentConsumer** processes payment
6. If payment OK → **PaymentCompletedEvent** to `notification-queue`
7. **NotificationConsumer** sends confirmation email
8. Order status updated to `COMPLETED`

## 🛠️ Technologies Used

| Technology       | Version | Purpose                |
| ---------------- | ------- | ---------------------- |
| Spring Boot      | 3.4.2   | Application framework  |
| Spring WebFlux   | 6.2.x   | Reactive web framework |
| Spring Cloud AWS | 3.3.0   | AWS integration        |
| R2DBC            | 1.0.x   | Reactive database      |
| H2 Database      | 2.x     | In-memory database     |
| Lombok           | 1.18.x  | Boilerplate reduction  |
| Jackson          | 2.x     | JSON processing        |

## 📝 Sample Data

The application pre-loads sample products via `schema.sql`:

| ID  | Name              | Price    | Stock |
| --- | ----------------- | -------- | ----- |
| 1   | iPhone 15 Pro     | $999.99  | 50    |
| 2   | MacBook Pro 16    | $2499.99 | 25    |
| 3   | AirPods Pro       | $249.99  | 100   |
| 4   | iPad Air          | $599.99  | 40    |
| 5   | Apple Watch Ultra | $799.99  | 30    |

## 🔒 Error Handling

- **Validation errors** → 400 Bad Request
- **Product not found** → 404 Not Found
- **Insufficient stock** → 409 Conflict
- **Internal errors** → 500 Internal Server Error

## 📈 Monitoring

Check application logs for event flow:

```
INFO  OrderController    : Received order creation request
INFO  OrderService       : Creating order: abc-123
INFO  EventPublisher     : Published OrderCreatedEvent
INFO  InventoryConsumer  : Received OrderCreatedEvent
INFO  InventoryService   : Reserved stock for product 1
INFO  EventPublisher     : Published StockReservedEvent
INFO  PaymentConsumer    : Processing payment
INFO  PaymentService     : Payment successful
INFO  EventPublisher     : Published PaymentCompletedEvent
INFO  NotificationConsumer: Processing notification
INFO  NotificationService: SENDING ORDER CONFIRMATION EMAIL
```

## 🚧 Future Enhancements

- [ ] Add Dead Letter Queue (DLQ) for failed messages
- [ ] Implement retry mechanism with exponential backoff
- [ ] Add distributed tracing (AWS X-Ray)
- [ ] Implement SAGA compensation for rollbacks
- [ ] Add metrics with Micrometer/CloudWatch
- [ ] Containerize with Docker
- [ ] Deploy to AWS ECS/EKS

## 📄 License

MIT License
