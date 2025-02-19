#!/bin/bash

# Variables
INFLUX_HOST="http://localhost:8086"    # InfluxDB Host

# Function to wait until InfluxDB is ready
wait_for_influx() {
  echo "Waiting for InfluxDB to start..."
  while ! curl -sI "${INFLUX_HOST}/ping" > /dev/null; do
    sleep 2
  done
  echo "InfluxDB is ready."
}

# Wait for InfluxDB to be ready
wait_for_influx

# Create the `jfr` database
echo "Creating database 'jfr'..."
curl -s -XPOST "${INFLUX_HOST}/query" \
  --data-urlencode "q=CREATE DATABASE jfr"

echo "Database 'jfr' created."

echo "Creating database 'gatling'..."
curl -s -XPOST "${INFLUX_HOST}/query" \
  --data-urlencode "q=CREATE DATABASE gatling"
echo "Database 'gatling' created."

DB_NAME="k6"  # Database name

# Create the `k6` database if it doesn't already exist
echo "Creating database '${DB_NAME}'..."
curl -s -XPOST "${INFLUX_HOST}/query" \
  --data-urlencode "q=CREATE DATABASE ${DB_NAME}" && echo "Database '${DB_NAME}' created."

# Create retention policy
echo "Creating retention policy '30d' on '${DB_NAME}'..."
curl -s -XPOST "${INFLUX_HOST}/query" \
  --data-urlencode "q=CREATE RETENTION POLICY \"30d\" ON ${DB_NAME} DURATION 30d REPLICATION 1 DEFAULT" && echo "Retention policy '30d' created."

# Create Continuous Query: failed RPS
echo "Creating continuous query 'k6_cq_failed_rps1'..."
curl -s -XPOST "${INFLUX_HOST}/query" \
  --data-urlencode "q=CREATE CONTINUOUS QUERY k6_cq_failed_rps1 ON \"${DB_NAME}\" BEGIN SELECT CUMULATIVE_SUM(count(\"duration\")) AS \"failed\" INTO \"k6-throughput\" FROM \"30d\".\"http_req_duration\" WHERE \"expected_response\" = 'false' GROUP BY time(1s),expected_response,testEnvironment,systemUnderTest END" && echo "Continuous query 'k6_cq_failed_rps1' created."

# Create Continuous Query: total RPS
echo "Creating continuous query 'k6_cq_total_rps'..."
curl -s -XPOST "${INFLUX_HOST}/query" \
  --data-urlencode "q=CREATE CONTINUOUS QUERY k6_cq_total_rps ON \"${DB_NAME}\" BEGIN SELECT CUMULATIVE_SUM(count(\"duration\")) AS \"total\" INTO \"k6-throughput\" FROM \"30d\".\"http_req_duration\" GROUP BY time(1s),expected_response,testEnvironment,systemUnderTest END" && echo "Continuous query 'k6_cq_total_rps' created."

# Create Continuous Query: response time percentiles
echo "Creating continuous query 'k6_cq_response_time_percentiles'..."
curl -s -XPOST "${INFLUX_HOST}/query" \
  --data-urlencode "q=CREATE CONTINUOUS QUERY k6_cq_response_time_percentiles ON \"${DB_NAME}\" BEGIN SELECT percentile(\"duration\", 50) AS \"cq_50pct_response_time\", percentile(\"duration\", 90) AS \"cq_90pct_response_time\", percentile(\"duration\", 95) AS \"cq_95pct_response_time\", percentile(\"duration\", 99) AS \"cq_99pct_response_time\", max(\"duration\") AS \"cq_max_response_time\", min(\"duration\") AS \"cq_min_response_time\", mean(\"duration\") AS \"cq_mean_response_time\", stddev(\"duration\") AS \"cq_stddev_response_time\", count(\"duration\") AS \"cq_request_count\" INTO \"k6-http-percentiles\" FROM \"30d\".\"http_req_duration\" GROUP BY time(1s),expected_response,testEnvironment,systemUnderTest,\"name\" END" && echo "Continuous query 'k6_cq_response_time_percentiles' created."
