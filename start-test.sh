#!/usr/bin/env bash

if [ ! -f scheduler/target/auto-resilience-test-1.0-SNAPSHOT.jar ]; then
  echo "Error: scheduler/target/auto-resilience-test-1.0-SNAPSHOT.jar is not found. Please build the project and try again."
  exit 1
fi

java -jar scheduler/target/auto-resilience-test-1.0-SNAPSHOT.jar
