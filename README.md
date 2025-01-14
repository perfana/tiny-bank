# Tiny Bank

This is a simple bank application that is used to demonstrate resilience patterns and performance testing.

## Explore Tiny Bank

To run the Tiny Bank application, follow these steps, which can also be run via the `start-test-components.sh` script:

1. Start the database and WireMock services:
    ```shell
    cd db
    docker compose up -d
    ```
2. Compile the Tiny Bank Spring Boot app from the `service` directory:
    ```shell
    cd service
    ../mvnw package
    ```
3. Start the wiremock services from the `service` directory:
    ```shell
   ./create-wiremock-jars.sh
   java -jar service/target/tiny-bank-service-0.0.1-account-stub-SNAPSHOT.jar &
   java -jar service/target/tiny-bank-service-0.0.1-balance-stub-SNAPSHOT.jar &
    ```
4. Start the Tiny Bank Spring Boot service from the `service` directory:
    ```shell
    cd service
    ../mvnw spring-boot:run
    ```
5. Start the Tiny Bank frontend from the `app` directory:
    ```shell
   cd app/tiny-fe
   npm install
   node app.js
    ```

The Tiny Bank application is now running and can be accessed at `http://localhost:13000`.

![initial-screen.png](images/initial-screen.png "Initial Screen")

## Overview

1. **Tiny Bank Spring Boot Application**  
   The core of the system is a Spring Boot application. It serves as the main service responsible for managing banking
   operations. Key features include:
    - **Retrieving account information**: The application connects to two external WireMock services to fetch account
      details and the current account balance for users.
    - **Transaction management**: The application interacts with a PostgreSQL database to fetch transaction
      information.

2. **WireMock Services**  
   Two WireMock servers are used to simulate external APIs:
    - One provides **account information**, such as user details or account metadata.
    - The other provides the **current account balance** for the respective accounts.
      These services allow for controlled testing and can simulate different response scenarios, like delays or errors,
      for resilience testing.

3. **PostgreSQL Database**  
   The database is designed to store information related to transactions. The Tiny Bank application uses this database
   to get the most recent transaction or the transaction overview.

4. **k6 Load Testing**
    - **k6** is a performance testing tool used to simulate user load on the Tiny Bank application.
    - This helps test the resilience and scalability of the system under heavy traffic conditions.
    - Care is taken that it is an "open system" so that the arrival rate of requests stays constant when the responses slow down.

5. **Event Scheduler**
    - The event scheduler is used to introduce delays intentionally for the WireMock services.
    - By slowing down responses from the external services, the system's behavior under such conditions can be
      evaluated, testing fault tolerance and timeout mechanisms.

Together, these components form a robust and testable system designed to demonstrate resilient patterns while handling
real-world scenarios like high load, service delays, and database dependencies.

## Observability

To observe the behavior of the system under load, metrics are collected using the following tools:

1. **Actuator Endpoints**  
   The Tiny Bank Spring Boot application exposes actuator endpoints with information about the application's health,
   metrics, and other details. These endpoints are used to monitor the application's status and performance.
2. **Prometheus** 
   Prometheus is used to store the Actuator metrics from the Spring Boot application. In the Tiny Bank set up the `otel-collector` 
   is used to scrape the metrics and send them to the Prometheus endpoint.
4. **Grafana** 
   Grafana is used to visualize the metrics collected by Prometheus. It provides dashboards that display the Tiny Bank
   performance and health in real-time.
5. **InfluxDB**
   InfluxDB is used to store metrics of k6 and JFR. It is a time-series database that stores data points
   for further analysis and visualization.
6. **Perfana** (optional)
   Perfana is used to analyse metrics from various sources, including k6 load tests, JFR, and other
   sources. It provides a unified view of the system's performance tests and helps identify bottlenecks and issues.

In the metrics folder there is a docker compose file that starts and provisions InfluxDB, Grafana, Prometheus and otel-collector.
See the [metrics README](metrics/README.md) for more information.

# Collect metrics

Extra tools are needed to collect metrics from the Tiny Bank application.
Use the `download-components.sh` script to download the necessary tools or follow instructions below.

## k6 metrics

The x2i tool is used to collect metrics from the k6 load test and send them to InfluxDB. The following steps are
needed to set up the x2i tool:

    wget https://github.com/perfana/x2i/releases/download/x2i-1.0.0/x2i-macos-arm64
    mv x2i-macos-arm64 x2i
    chmod u+x x2i

## Java Flight Recorder (JFR) metrics

The JFR exporter Java agent is used to send JFR data to InfluxDB. The following steps are needed to set up the JFR exporter:

    wget https://github.com/perfana/jfr-exporter/releases/download/0.5.0/jfr-exporter-0.5.0.jar
    mv jfr-exporter-0.5.0.jar jfr-exporter.jar

Add the Java JFR exporter agent to the JVM options of the Tiny Bank Spring Boot application:

    -javaagent:jfr-exporter.jar=influxUrl=http://localhost:8086,influxDatabase=jfr,tag=service/tiny-bank-service,tag=systemUnderTest/tiny-bank,tag=testEnvironment/silver -XX:NativeMemoryTracking=summary

# Running a Resilience Test

To run the Tiny Bank resilience test, make sure x2i and jfr-exporter jar are available in the root directory of this project.
Make sure docker is available to start the docker compose files.

The following steps are needed to run the resilience test:
1. Start the metrics components using the docker compose file in the metrics folder.
   ```shell
   cd metrics
   docker compose up -d
   ```
2. Compile the test run scheduler.
    ```shell
    ./mvnw clean package
    ```
3. Run the test run scheduler from the root directory of this project.
    ```shell
    java -jar scheduler/target/auto-resilience-test-1.0-SNAPSHOT.jar
    ```
   
You can check the results in Grafana by logging in with `admin`/`admin` at `http://localhost:3000`. 
Select the k6 dashboard to see the k6 metrics and the JFR dashboard to see the JFR metrics.

## Troubleshooting

If database issues occur, you can reset the database by running the following command from the root directory:
```shell
cd db
docker compose down --volumes
```
Beware that `--volumes` will remove the volumes, so data will be lost. The Tiny Bank application will be reset to its initial state
when restarting the Spring Boot application.

Stop all components by running the `stop-test.sh` script in the root directory of this project.
