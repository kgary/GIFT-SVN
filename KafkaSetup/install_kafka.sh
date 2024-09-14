#!/bin/bash

# Define Kafka version and download URL
# KAFKA_VERSION="3.8.0"
KAFKA_URL="https://downloads.apache.org/kafka/3.8.0/kafka_2.13-3.8.0.tgz"

# Directory to install Kafka
INSTALL_DIR="$HOME/kafka"

# Download and extract Kafka if not already installed
if [ ! -d "$INSTALL_DIR" ]; then
    echo "Downloading Kafka..."
    mkdir -p $INSTALL_DIR
    curl -L $KAFKA_URL -o kafka.tgz
    tar -xzf kafka.tgz -C $INSTALL_DIR --strip-components 1
    rm kafka.tgz
fi

# Update paths in server.properties and zookeeper.properties
sed -i 's|log.dirs=/tmp/kafka-logs|log.dirs='"$INSTALL_DIR"'/kafka-logs|' $INSTALL_DIR/config/server.properties
sed -i 's|dataDir=/tmp/zookeeper|dataDir='"$INSTALL_DIR"'/zookeeper-data|' $INSTALL_DIR/config/zookeeper.properties

# Export Kafka bin path to the system PATH
echo "export PATH=\$PATH:$INSTALL_DIR/bin" >> ~/.bashrc
source ~/.bashrc

echo "Kafka installed successfully!"
