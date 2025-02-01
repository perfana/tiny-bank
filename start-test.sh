#!/usr/bin/env bash

if [ ! -f scheduler/target/auto-resilience-test-1.0-SNAPSHOT.jar ]; then
  echo "Error: scheduler/target/auto-resilience-test-1.0-SNAPSHOT.jar is not found. Please build the project and try again."
  exit 1
fi

echo "Starting test"

# check if PERFANA_API_KEY environment variable is set, otherwise report skipping Perfana events.
if [ -z "$PERFANA_API_KEY" ]; then
  echo "Environment variable PERFANA_API_KEY is not set. Skipping Perfana events."
fi

if [ -z "$PERFANA_URL" ] && [ "$PERFANA_API_KEY" != "" ]; then
  echo "Environment variable PERFANA_URL is not set. Using default."
fi

if [ -z "$INFLUX_URL" ]; then
  echo "Environment variable INFLUX_URL is not set. Using default."
fi

if [ -z "$INFLUX_DB" ]; then
  echo "Environment variable INFLUX_DB is not set. Using default."
fi

if [ -z "$INFLUX_USER" ]; then
  echo "Environment variable INFLUX_USER is not set. Using default."
fi

if [ -z "$INFLUX_PASSWORD" ]; then
  echo "Environment variable INFLUX_PASSWORD is not set. Using default."
fi

read -p "Press Enter to start the load test scheduler... (or press Ctrl+C to cancel)"

java -jar scheduler/target/auto-resilience-test-1.0-SNAPSHOT.jar
