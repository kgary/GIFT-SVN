#!/bin/bash

INSTALL_DIR="$HOME/kafka"

# Start Zookeeper
echo "Starting Zookeeper..."
$INSTALL_DIR/bin/zookeeper-server-start.sh -daemon $INSTALL_DIR/config/zookeeper.properties

# Start Kafka server
echo "Starting Kafka server..."
$INSTALL_DIR/bin/kafka-server-start.sh -daemon $INSTALL_DIR/config/server.properties

# Wait for Kafka to start
sleep 2

echo "Kafka and Zookeeper started"



