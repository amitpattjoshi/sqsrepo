# Terraform variables for E-Commerce SQS
# Customize these values for your environment

aws_region  = "eu-west-1"
aws_profile = "sqs"
environment = "dev" # Leave empty for simple names like "order-queue.fifo"

common_tags = {
  Project     = "ecommerce-sqs"
  ManagedBy   = "terraform"
  Application = "ecommerce"
  Environment = "development"
}
