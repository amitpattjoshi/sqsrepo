variable "instance_name" {
  description = "Name tag for the EC2 instance"
  type        = string
  default     = "ecommerce-sqs-app"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.micro"
}

variable "ami_id" {
  description = "Custom AMI ID. Leave empty to use latest Amazon Linux 2023"
  type        = string
  default     = ""
}

variable "key_name" {
  description = "SSH key pair name. Leave null for SSM-only access"
  type        = string
  default     = null
}

variable "vpc_id" {
  description = "VPC ID where the instance will be launched"
  type        = string
}

variable "subnet_id" {
  description = "Subnet ID for the instance"
  type        = string
}

variable "app_port" {
  description = "Application port to open in security group"
  type        = number
  default     = 8082
}

variable "enable_ssh" {
  description = "Whether to open SSH port 22"
  type        = bool
  default     = false
}

variable "ssh_cidr_blocks" {
  description = "CIDR blocks allowed for SSH access"
  type        = list(string)
  default     = []
}

variable "ingress_cidr_blocks" {
  description = "CIDR blocks allowed for application port access"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "root_volume_size" {
  description = "Root EBS volume size in GB"
  type        = number
  default     = 30
}

variable "user_data" {
  description = "Custom user data script. Leave empty for default Java 21 setup"
  type        = string
  default     = ""
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default     = {}
}
