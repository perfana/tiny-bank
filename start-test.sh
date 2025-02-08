#!/usr/bin/env bash

./mvnw package -DskipTests

if [ ! -f scheduler/target/auto-resilience-test-1.0-SNAPSHOT.jar ]; then
  echo "Error: scheduler/target/auto-resilience-test-1.0-SNAPSHOT.jar is not found. Please build the project and try again."
  exit 1
fi

echo "Starting test"

# note: the following environment variables are used in the test Scheduler
# here: src/main/java/io/perfana/scheduler/TestScheduler.java

# check if PERFANA_API_KEY environment variable is set, otherwise report skipping Perfana events.
if [ -z "$PERFANA_API_KEY" ]; then
  echo "Environment variable PERFANA_API_KEY is not set. Skipping Perfana events."
fi

if [ -z "$PERFANA_URL" ] && [ "$PERFANA_API_KEY" != "" ]; then
  echo "Environment variable PERFANA_URL is not set. Using default."
else
  echo "PERFANA_API_KEY is set, using Perfana URL: $PERFANA_URL"
fi

if [ -z "$INFLUX_URL" ]; then
  echo "Environment variable INFLUX_URL is not set. Using default."
else
  echo "Influx URL: $INFLUX_URL"
fi

if [ -z "$INFLUX_DB_K6" ]; then
  echo "Environment variable INFLUX_DB_K6 is not set. Using default."
else
  echo "Influx DB K6: $INFLUX_DB_K6"
fi

if [ -z "$INFLUX_DB_JFR" ]; then
  echo "Environment variable INFLUX_DB_JFR is not set. Using default."
else
  echo "Influx DB K6: $INFLUX_DB_JFR"
fi

if [ -z "$INFLUX_USER" ]; then
  echo "Environment variable INFLUX_USER is not set. Using default."
else
  echo "Influx User: $INFLUX_USER"
fi

if [ -z "$INFLUX_PASSWORD" ]; then
  echo "Environment variable INFLUX_PASSWORD is not set. Using default."
else
  echo "Influx Password: ***"
fi

if [ -z "$JFR_AGENT" ]; then
  echo "Environment variable JFR_AGENT is not set. Using default."
else
  echo "JFR Agent: $JFR_AGENT"
fi

if [ -z "$OTEL_AGENT" ]; then
  echo "Environment variable OTEL_AGENT is not set. Using default."
else
  echo "OTEL Agent: $OTEL_AGENT"
fi

read -p "Press Enter to start the load test scheduler... (or press Ctrl+C to cancel)"

java -jar scheduler/target/auto-resilience-test-1.0-SNAPSHOT.jar "$@"
