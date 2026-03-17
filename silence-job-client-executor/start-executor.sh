#!/bin/bash

# SilenceJob Client Executor Startup Script
# 用于启动独立的客户端执行器应用

echo "=========================================="
echo "SilenceJob Client Executor Startup Script"
echo "=========================================="

# Default configuration
JAR_FILE="target/silence-job-client-executor-1.5.0.jar"
JAVA_OPTS="-Xmx512m -Xms256m"
SPRING_PROFILE="dev"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --prod)
      SPRING_PROFILE="prd"
      shift
      ;;
    --jar=*)
      JAR_FILE="${1#*=}"
      shift
      ;;
    --java-opts=*)
      JAVA_OPTS="${1#*=}"
      shift
      ;;
    --help)
      echo "Usage: $0 [OPTIONS]"
      echo "Options:"
      echo "  --prod          Use production profile"
      echo "  --jar=FILE      Specify JAR file path (default: target/silence-job-client-executor-1.5.0.jar)"
      echo "  --java-opts=OPTS Specify JVM options (default: -Xmx512m -Xms256m)"
      echo "  --help          Show this help message"
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      echo "Use --help for usage information"
      exit 1
      ;;
  esac
done

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found: $JAR_FILE"
    echo "Please run 'mvn clean package' first"
    exit 1
fi

echo "Starting SilenceJob Client Executor..."
echo "JAR File: $JAR_FILE"
echo "Profile: $SPRING_PROFILE"
echo "JVM Options: $JAVA_OPTS"
echo "=========================================="

# Start the application
java $JAVA_OPTS -jar $JAR_FILE --spring.profiles.active=$SPRING_PROFILE