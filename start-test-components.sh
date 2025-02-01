#!/bin/bash

DEBUG=false

# Check if --debug flag is provided
for arg in "$@"; do
  if [ "$arg" == "--debug" ]; then
    DEBUG=true
    break
  fi
done

# Set up output redirection based on the --debug flag
if [ "$DEBUG" = true ]; then
  OUT="/dev/stdout"
  ERR="/dev/stderr"
else
  OUT="/dev/null"
  ERR="/dev/null"
fi

# Check if Docker is installed
if ! command -v docker > /dev/null 2>&1; then
  echo "Error: Docker is not installed. Please install Docker and try again."
  exit 1
fi

# Verify if Docker is running
if ! docker ps > /dev/null 2>&1; then
  echo "Error: Docker is not running. Please start Docker and try again."
  exit 1
fi

# Check if Java is installed
if ! command -v java > /dev/null 2>&1; then
  echo "Error: Java is not installed. Please install Java 21+ and try again."
  exit 1
fi

# Check if x2i and jfr-exporter.jar are available
if [ ! -f x2i ] || [ ! -f jfr-exporter.jar ]; then
  echo "Downloading x2i or jfr-exporter.jar."
  download-components.sh
fi

echo "Building app"
./mvnw package -DskipTests > $OUT 2>$ERR

echo "Starting stubs"
./create-wiremock-jars.sh > $OUT 2>$ERR
java -jar service/target/tiny-bank-service-0.0.1-account-stub-SNAPSHOT.jar > $OUT 2>$ERR &
java -jar service/target/tiny-bank-service-0.0.1-balance-stub-SNAPSHOT.jar > $OUT 2>$ERR &

echo "Starting database"
cd db
docker compose up -d || { echo "Error: Failed to start database using Docker Compose."; exit 1; }
cd - > $OUT 2>$ERR

echo "Waiting for database to start"
sleep 3

echo "Starting and provisioning Influxdb and Grafana"
cd metrics
docker compose up -d || { echo "Error: Failed to start Grafana using Docker Compose."; exit 1; }
cd - > $OUT 2>$ERR

echo "Starting services"
INFLUX_URL=${INFLUX_URL:-http://localhost:8086}
INFLUX_DB=${INFLUX_DB:-jfr}
nohup java -javaagent:jfr-exporter.jar=influxUrl=$INFLUX_URL,influxDatabase=$INFLUX_DB,tag=service/tiny-bank-service,tag=systemUnderTest/tiny-bank,tag=testEnvironment/silver \
                    -XX:NativeMemoryTracking=summary \
                    -Xmx1g -jar service/target/tiny-bank-service-0.0.1-SNAPSHOT.jar > $OUT 2>$ERR &

echo "Starting tiny-fe"
cd app/tiny-fe
npm install > $OUT 2>$ERR
node app.js > $OUT 2>$ERR &
cd - > $OUT 2>$ERR

echo "Open the tiny-bank app at http://localhost:13000 and use the given user ids"
echo "Open the Grafana at http://localhost:3000 and login with admin/admin"

sleep 6

declare services=(
  [18080]="tiny-bank-service"
  [13000]="tiny-fe"
  [3000]="Grafana"
  [30123]="Account stub"
  [30124]="Balance stub"
)

for port in "${!services[@]}"; do
  if ! nc -z localhost "$port"; then
    echo "Error: ${services[$port]} is not running. Please check the logs and try again."
    exit 1
  fi
done

echo "All services are up and running."