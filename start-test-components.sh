#!/bin/bash

# note: <&- >&- 2>&- is used to close stdin, stdout and stderr
# otherwise the process will hang until timeout
# need to look into this further, as logging from these processes is now disabled

echo "Building app"
./mvnw package -DskipTests > /dev/null 2>&1

echo "Starting stubs"
./create-wiremock-jars.sh
java -jar service/target/tiny-bank-service-0.0.1-account-stub-SNAPSHOT.jar <&- >&- 2>&- &
java -jar service/target/tiny-bank-service-0.0.1-balance-stub-SNAPSHOT.jar <&- >&- 2>&- &

echo "Starting database"
cd db
docker compose up -d > /dev/null 2>&1
cd - > /dev/null 2>&1

echo "Waiting for database to start"
sleep 3

echo "Starting services"
nohup java -javaagent:jfr-exporter.jar=influxUrl=http://localhost:8086,influxDatabase=jfr,tag=service/tiny-bank-service,tag=systemUnderTest/tiny-bank,tag=testEnvironment/silver \
                    -XX:NativeMemoryTracking=summary \
                    -Xmx1g -jar service/target/tiny-bank-service-0.0.1-SNAPSHOT.jar <&- >&- 2>&- &

echo "Starting tiny-fe"
cd app/tiny-fe
npm install > /dev/null 2>&1
node app.js <&- >&- 2>&- &
cd - > /dev/null 2>&1

sleep 6

