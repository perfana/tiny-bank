#!/usr/bin/env bash

for port in {30123,30124,18080,13000};
do
  # Get the process IDs listening on port
  pids=$(lsof -t -i:$port -sTCP:LISTEN)

  if [ -n "$pids" ]; then
    echo "Port $port - Found PIDs: $(echo $pids | tr '\n' ' ')"

    for pid in $pids; do
      # Prevent killing the script itself or its parent process
      if [ "$pid" -ne "$$" ] && [ "$pid" -ne "$PPID" ]; then
        echo "Port $port - Killing process ID $pid"
        kill "$pid"
      else
        echo "Port $port - Skipping process ID $pid (script or parent process)"
      fi
    done
  else
    echo "Port $port - No process"
  fi
done

echo "Check for hanging tiny-bank-services (in case Java process is running but not listening on a port)"
ps aux | grep "tiny-bank-service-0.0.1-SNAPSHOT.ja[r]" | awk '{print $2}' | xargs -I {} kill -9 {}

# Stop the database
cd db
docker compose down
cd - > /dev/null 2>&1

# Check if --shutdown-metrics flag is present: stop the metrics components
if [[ " $@ " == *" --shutdown-metrics "* ]]; then
  cd metrics
  docker compose down
  cd - > /dev/null 2>&1
fi
