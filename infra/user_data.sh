#!/bin/bash
set -e

# Make scripts executable
chmod +x /home/ec2-user/install_jenkins.sh
chmod +x /home/ec2-user/setup_appdir.sh

# Run scripts
/home/ec2-user/install_jenkins.sh
/home/ec2-user/setup_appdir.sh

echo "All setup scripts executed!"