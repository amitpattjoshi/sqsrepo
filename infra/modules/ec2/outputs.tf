output "instance_id" {
  description = "EC2 instance ID"
  value       = aws_instance.this.id
}

output "public_ip" {
  description = "Public IP of the instance (if assigned)"
  value       = aws_instance.this.public_ip
}

output "private_ip" {
  description = "Private IP of the instance"
  value       = aws_instance.this.private_ip
}

output "security_group_id" {
  description = "Security group ID"
  value       = aws_security_group.ec2_sg.id
}

output "iam_role_arn" {
  description = "IAM role ARN attached to the instance"
  value       = aws_iam_role.ec2_role.arn
}

output "instance_profile_name" {
  description = "Instance profile name"
  value       = aws_iam_instance_profile.ec2_profile.name
}
