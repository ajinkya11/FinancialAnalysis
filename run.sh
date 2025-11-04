#!/bin/bash

# Airline Financial Analyzer - Run Script
# Usage: ./run.sh [command] [arguments]

set -e

JAR_FILE="target/airline-financial-analyzer-1.0.0.jar"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${RED}Error: JAR file not found!${NC}"
    echo "Please build the project first:"
    echo "  mvn clean package"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}Error: Java 17 or higher is required${NC}"
    echo "Current Java version: $JAVA_VERSION"
    exit 1
fi

# Run the application
echo -e "${GREEN}Starting Airline Financial Analyzer...${NC}"
java -jar "$JAR_FILE" "$@"
