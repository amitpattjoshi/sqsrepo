#!/bin/bash
set -e

# Install Java 21
sudo yum install -y java-21-amazon-corretto-headless

# Add Jenkins repo and install Jenkins
sudo wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
sudo rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io-2023.key
sudo yum upgrade -y
sudo yum install -y jenkins
sudo systemctl enable jenkins
sudo systemctl start jenkins

echo "Jenkins installation complete!"