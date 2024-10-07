#!/bin/bash

INSTALL_DIR="$HOME/kafka"

# Create the Kafka topic
TOPIC_NAME="scenario-topic"
$INSTALL_DIR/bin/kafka-topics.sh --create --topic $TOPIC_NAME --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

echo "Topic '$TOPIC_NAME' created."