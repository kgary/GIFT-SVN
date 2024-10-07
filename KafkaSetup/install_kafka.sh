#!/bin/bash

# Define Kafka version and download URL
KAFKA_URL="https://downloads.apache.org/kafka/3.8.0/kafka_2.13-3.8.0.tgz"

# Directory to install Kafka
INSTALL_DIR="$HOME/kafka"

# Convert Unix-style paths to Windows-style (C:/...)
WIN_INSTALL_DIR=$(echo "$INSTALL_DIR" | sed 's|/c|C:|; s|/|\\|g')

# Download and extract Kafka if not already installed
if [ ! -d "$INSTALL_DIR" ]; then
    echo "Downloading Kafka..."
    mkdir -p "$INSTALL_DIR"
    curl -L "$KAFKA_URL" -o kafka.tgz
    tar -xzf kafka.tgz -C "$INSTALL_DIR" --strip-components 1
    rm kafka.tgz
fi

# Set the Kafka log directory inside INSTALL_DIR
LOG_DIR="${WIN_INSTALL_DIR}\\kafka-logs"
DATA_DIR="${WIN_INSTALL_DIR}\\zookeeper-data"

# Properly format the paths using forward slashes and ensure double backslashes
# Update paths in server.properties and zookeeper.properties
sed -i "s|log.dirs=/tmp/kafka-logs|log.dirs=${LOG_DIR//\\/\/}|" "$INSTALL_DIR/config/server.properties"
sed -i "s|dataDir=/tmp/zookeeper|dataDir=${DATA_DIR//\\/\/}|" "$INSTALL_DIR/config/zookeeper.properties"

# Get the machine's IP address using PowerShell
IP_ADDRESS=$(powershell -Command "(Get-NetIPAddress -AddressFamily IPv4 -InterfaceAlias 'Ethernet' | Select-Object -First 1).IPAddress")

# Update server.properties with the correct IP address for listeners and advertised.listeners
sed -i "s|^#listeners=PLAINTEXT://:9092|listeners=PLAINTEXT://0.0.0.0:9092|" "$INSTALL_DIR/config/server.properties"
sed -i "s|^#advertised.listeners=PLAINTEXT://your.host.name:9092|advertised.listeners=PLAINTEXT://${IP_ADDRESS}:9092|" "$INSTALL_DIR/config/server.properties"

# Export Kafka bin path to the system PATH
echo "export PATH=\$PATH:$INSTALL_DIR/bin" >> ~/.bashrc
source ~/.bashrc

echo "Kafka installed and configured successfully!"
