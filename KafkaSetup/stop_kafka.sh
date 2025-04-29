#!/bin/bash

echo "Stopping Kafka and Zookeeper..."

stop_process() {
    local process_name=$1
    local pids=$(jps -l | grep "$process_name" | awk '{print $1}')
    if [ -n "$pids" ]; then
        echo "Found $process_name (PIDs: $pids)"
        for pid in $pids; do
            echo "Stopping $process_name (PID: $pid)..."
            taskkill //PID $pid //F
        done
    else
        echo "No $process_name process found."
    fi
}

stop_process "kafka.Kafka"
stop_process "org.apache.zookeeper.server.quorum.QuorumPeerMain"

echo "Kafka and Zookeeper stop attempts completed."

echo "Checking for any remaining Kafka or Zookeeper processes..."
remaining_processes=$(jps -l | grep -E "kafka.Kafka|org.apache.zookeeper.server.quorum.QuorumPeerMain")
if [ -n "$remaining_processes" ]; then
    echo "Warning: Some processes are still running:"
    echo "$remaining_processes"
else
    echo "No Kafka or Zookeeper processes found. All stopped successfully."
fi

echo "JPS output:"
jps -l