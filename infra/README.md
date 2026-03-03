# E-Commerce SQS Infrastructure

Terraform configuration for creating AWS SQS FIFO queues for the e-commerce application.

## Prerequisites

- [Terraform](https://www.terraform.io/downloads.html) >= 1.0.0
- AWS CLI configured with profile `sqs`
- Appropriate AWS permissions to create SQS queues

## Queues Created

| Queue                     | Purpose                   | Dead Letter Queue       |
| ------------------------- | ------------------------- | ----------------------- |
| `order-queue.fifo`        | Receives new order events | `order-dlq.fifo`        |
| `inventory-queue.fifo`    | Stock reservation events  | `inventory-dlq.fifo`    |
| `payment-queue.fifo`      | Payment processing events | `payment-dlq.fifo`      |
| `notification-queue.fifo` | Customer notifications    | `notification-dlq.fifo` |

## Usage

### Initialize Terraform

```bash
cd infra
terraform init
```

### Preview Changes

```bash
terraform plan
```

### Apply Changes (Create Queues)

```bash
terraform apply
```

### Destroy Resources

```bash
terraform destroy
```

## Configuration

Edit `terraform.tfvars` to customize:

```hcl
aws_region  = "eu-west-1"    # Your AWS region
aws_profile = "sqs"          # Your AWS CLI profile
environment = "dev"          # Environment prefix (optional)
```

## Outputs

After applying, Terraform will output:

- Queue URLs
- Queue ARNs
- Queue names (for `application.yml`)

### Update Application Configuration

After running Terraform, update your `application.yml` with the queue names:

```yaml
app:
  sqs:
    queues:
      order: order-queue.fifo
      payment: payment-queue.fifo
      notification: notification-queue.fifo
      inventory: inventory-queue.fifo
```

## Architecture

```
┌─────────────────┐     ┌─────────────────┐
│  Order Service  │────▶│  order-queue    │
└─────────────────┘     └────────┬────────┘
                                 │
                                 ▼
┌─────────────────┐     ┌─────────────────┐
│Inventory Service│◀────│ inventory-queue │
└─────────────────┘     └────────┬────────┘
                                 │
                                 ▼
┌─────────────────┐     ┌─────────────────┐
│ Payment Service │◀────│  payment-queue  │
└─────────────────┘     └────────┬────────┘
                                 │
                                 ▼
┌─────────────────┐     ┌─────────────────┐
│Notification Svc │◀────│notification-que │
└─────────────────┘     └─────────────────┘
```

## Dead Letter Queues

Each queue has an associated DLQ with:

- 3 retry attempts before moving to DLQ
- 14-day message retention in DLQ
- FIFO ordering preserved
