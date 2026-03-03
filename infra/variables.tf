
variable "aws_region" {
  description = "AWS region for resources"
  type        = string
  default     = "eu-west-1"
}
variable "aws_profile" {
  description = "AWS CLI profile to use"
  type        = string
  default     = "sqs"
}

variable "ec2_instance_name" {
  description = "Name for the EC2 instance"
  type        = string
  default     = "ecommerce-sqs-app"
}
variable "ec2_enable_ssh" {
  type    = bool
  default = true
}
variable "ec2_ssh_cidr_blocks" {
  type    = list(string)
  default = []
}
variable "ec2_key_name" {
  type    = string
  default = null
}

variable "environment" {
  description = "Environment name (e.g., dev, staging, prod)"
  type        = string
  default     = ""
}

variable "common_tags" {
  description = "Common tags to apply to all resources"
  type        = map(string)
  default = {
    Project     = "ecommerce-sqs"
    ManagedBy   = "terraform"
    Application = "ecommerce"
  }
}
