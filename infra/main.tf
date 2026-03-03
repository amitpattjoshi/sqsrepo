terraform {
  required_version = ">=1.0.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~>5.0"
    }
  }
}

#aws provider configuration
provider "aws" {
  region  = var.aws_region
  profile = var.aws_profile
}


# Local value for queue name prefix
locals {
  name_prefix = var.environment != "" ? "${var.environment}-" : ""
}


# Order Queue - For receiving new orders
resource "aws_sqs_queue" "order_queue" {
  name                        = "${local.name_prefix}order-queue.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  deduplication_scope         = "messageGroup"
  fifo_throughput_limit       = "perMessageGroupId"

  # Server-side encryption (SSE-SQS)
  sqs_managed_sse_enabled = true

  # Visibility timeout (30 seconds)
  visibility_timeout_seconds = 30

  # Message retention (4 days)
  message_retention_seconds = 345600

  # Max message size (256 KB)
  max_message_size = 262144

  # Receive wait time (long polling - 20 seconds)
  receive_wait_time_seconds = 20

  # Dead letter queue
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.order_dlq.arn
    maxReceiveCount     = 3
  })

  tags = var.common_tags
}


# Order Dead Letter Queue
resource "aws_sqs_queue" "order_dlq" {
  name                        = "${local.name_prefix}order-dlq.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  sqs_managed_sse_enabled     = true
  message_retention_seconds   = 1209600 # 14 days

  tags = var.common_tags
}
# Inventory Queue - For stock reservation
resource "aws_sqs_queue" "inventory_queue" {
  name                        = "${local.name_prefix}inventory-queue.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  deduplication_scope         = "messageGroup"
  fifo_throughput_limit       = "perMessageGroupId"
  sqs_managed_sse_enabled     = true

  visibility_timeout_seconds = 30
  message_retention_seconds  = 345600
  max_message_size           = 262144
  receive_wait_time_seconds  = 20

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.inventory_dlq.arn
    maxReceiveCount     = 3
  })

  tags = var.common_tags
}
# Inventory Dead Letter Queue
resource "aws_sqs_queue" "inventory_dlq" {
  name                        = "${local.name_prefix}inventory-dlq.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  sqs_managed_sse_enabled     = true
  message_retention_seconds   = 1209600

  tags = var.common_tags
}
# Payment Queue - For payment processing
resource "aws_sqs_queue" "payment_queue" {
  name                        = "${local.name_prefix}payment-queue.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  deduplication_scope         = "messageGroup"
  fifo_throughput_limit       = "perMessageGroupId"
  sqs_managed_sse_enabled     = true

  visibility_timeout_seconds = 60 # Longer for payment processing
  message_retention_seconds  = 345600
  max_message_size           = 262144
  receive_wait_time_seconds  = 20

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.payment_dlq.arn
    maxReceiveCount     = 3
  })

  tags = var.common_tags
}
# Payment Dead Letter Queue
resource "aws_sqs_queue" "payment_dlq" {
  name                        = "${local.name_prefix}payment-dlq.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  sqs_managed_sse_enabled     = true
  message_retention_seconds   = 1209600

  tags = var.common_tags
}

# Notification Queue - For sending notifications
resource "aws_sqs_queue" "notification_queue" {
  name                        = "${local.name_prefix}notification-queue.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  deduplication_scope         = "messageGroup"
  fifo_throughput_limit       = "perMessageGroupId"
  sqs_managed_sse_enabled     = true

  visibility_timeout_seconds = 30
  message_retention_seconds  = 345600
  max_message_size           = 262144
  receive_wait_time_seconds  = 20

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.notification_dlq.arn
    maxReceiveCount     = 3
  })

  tags = var.common_tags
}

# Notification Dead Letter Queue
resource "aws_sqs_queue" "notification_dlq" {
  name                        = "${local.name_prefix}notification-dlq.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  sqs_managed_sse_enabled     = true
  message_retention_seconds   = 1209600

  tags = var.common_tags
}

data "aws_caller_identity" "current" {}
resource "aws_sqs_queue_policy" "order_queue_policy" {
  queue_url = aws_sqs_queue.order_queue.url

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowSameAccountAccess"
        Effect    = "Allow"
        Principal = { AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root" }
        Action    = ["sqs:SendMessage", "sqs:ReceiveMessage", "sqs:DeleteMessage", "sqs:GetQueueAttributes"]
        Resource  = aws_sqs_queue.order_queue.arn
      },
      {
        Sid       = "DenyInsecureTransport"
        Effect    = "Deny"
        Principal = "*"
        Action    = "sqs:*"
        Resource  = aws_sqs_queue.order_queue.arn
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

resource "aws_sqs_queue_policy" "inventory_queue_policy" {
  queue_url = aws_sqs_queue.inventory_queue.url

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowSameAccountAccess"
        Effect    = "Allow"
        Principal = { AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root" }
        Action    = ["sqs:SendMessage", "sqs:ReceiveMessage", "sqs:DeleteMessage", "sqs:GetQueueAttributes"]
        Resource  = aws_sqs_queue.inventory_queue.arn
      },
      {
        Sid       = "DenyInsecureTransport"
        Effect    = "Deny"
        Principal = "*"
        Action    = "sqs:*"
        Resource  = aws_sqs_queue.inventory_queue.arn
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

resource "aws_sqs_queue_policy" "payment_queue_policy" {
  queue_url = aws_sqs_queue.payment_queue.url

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowSameAccountAccess"
        Effect    = "Allow"
        Principal = { AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root" }
        Action    = ["sqs:SendMessage", "sqs:ReceiveMessage", "sqs:DeleteMessage", "sqs:GetQueueAttributes"]
        Resource  = aws_sqs_queue.payment_queue.arn
      },
      {
        Sid       = "DenyInsecureTransport"
        Effect    = "Deny"
        Principal = "*"
        Action    = "sqs:*"
        Resource  = aws_sqs_queue.payment_queue.arn
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

resource "aws_sqs_queue_policy" "notification_queue_policy" {
  queue_url = aws_sqs_queue.notification_queue.url

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowSameAccountAccess"
        Effect    = "Allow"
        Principal = { AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root" }
        Action    = ["sqs:SendMessage", "sqs:ReceiveMessage", "sqs:DeleteMessage", "sqs:GetQueueAttributes"]
        Resource  = aws_sqs_queue.notification_queue.arn
      },
      {
        Sid       = "DenyInsecureTransport"
        Effect    = "Deny"
        Principal = "*"
        Action    = "sqs:*"
        Resource  = aws_sqs_queue.notification_queue.arn
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}
# ============================================
# EC2 Instance Module
# ============================================

# Use default VPC and subnet
data "aws_vpc" "default" {
  default = "true"
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
  filter {
    name   = "default-for-az"
    values = ["true"]
  }
}

module "ec2" {
  source          = "./modules/ec2"
  instance_name   = var.ec2_instance_name
  vpc_id          = data.aws_vpc.default.id
  subnet_id       = data.aws_subnets.default.ids[0]
  enable_ssh      = var.ec2_enable_ssh
  ssh_cidr_blocks = var.ec2_ssh_cidr_blocks
  key_name        = var.ec2_key_name

  tags = var.common_tags
}
