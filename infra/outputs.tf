# Outputs for E-Commerce SQS Infrastructure

# Order Queue Outputs
output "order_queue_url" {
  description = "URL of the order queue"
  value       = aws_sqs_queue.order_queue.url
}

output "order_queue_arn" {
  description = "ARN of the order queue"
  value       = aws_sqs_queue.order_queue.arn
}

output "order_queue_name" {
  description = "Name of the order queue"
  value       = aws_sqs_queue.order_queue.name
}

# Inventory Queue Outputs
output "inventory_queue_url" {
  description = "URL of the inventory queue"
  value       = aws_sqs_queue.inventory_queue.url
}

output "inventory_queue_arn" {
  description = "ARN of the inventory queue"
  value       = aws_sqs_queue.inventory_queue.arn
}

output "inventory_queue_name" {
  description = "Name of the inventory queue"
  value       = aws_sqs_queue.inventory_queue.name
}

# Payment Queue Outputs
output "payment_queue_url" {
  description = "URL of the payment queue"
  value       = aws_sqs_queue.payment_queue.url
}

output "payment_queue_arn" {
  description = "ARN of the payment queue"
  value       = aws_sqs_queue.payment_queue.arn
}

output "payment_queue_name" {
  description = "Name of the payment queue"
  value       = aws_sqs_queue.payment_queue.name
}

# Notification Queue Outputs
output "notification_queue_url" {
  description = "URL of the notification queue"
  value       = aws_sqs_queue.notification_queue.url
}

output "notification_queue_arn" {
  description = "ARN of the notification queue"
  value       = aws_sqs_queue.notification_queue.arn
}

output "notification_queue_name" {
  description = "Name of the notification queue"
  value       = aws_sqs_queue.notification_queue.name
}

# # Dead Letter Queue Outputs
output "dlq_arns" {
  description = "ARNs of all dead letter queues"
  value = {
    order        = aws_sqs_queue.order_dlq.arn
    inventory    = aws_sqs_queue.inventory_dlq.arn
    payment      = aws_sqs_queue.payment_dlq.arn
    notification = aws_sqs_queue.notification_dlq.arn
  }
}

# # All Queue Names (for application.yml reference)
output "queue_names" {
  description = "All queue names for application configuration"
  value = {
    order        = aws_sqs_queue.order_queue.name
    inventory    = aws_sqs_queue.inventory_queue.name
    payment      = aws_sqs_queue.payment_queue.name
    notification = aws_sqs_queue.notification_queue.name
  }
}

# ---- EC2 Outputs ----

output "ec2_instance_id" {
  description = "EC2 instance ID"
  value       = module.ec2.instance_id
}

output "ec2_public_ip" {
  description = "EC2 public IP"
  value       = module.ec2.public_ip
}

output "ec2_private_ip" {
  description = "EC2 private IP"
  value       = module.ec2.private_ip
}
