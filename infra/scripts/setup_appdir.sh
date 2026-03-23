#!/bin/bash
set -e

# Create application directory and set permissions
sudo mkdir -p /opt/ecommerce
sudo chown ec2-user:ec2-user /opt/ecommerce

echo "/opt/ecommerce directory created and permissions set!"