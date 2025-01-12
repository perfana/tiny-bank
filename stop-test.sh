#!/usr/bin/env bash

for port in {30123,30124,18080,13000};
do
  # Get the process IDs for the port (could be multiple PIDs)
  pids=$(lsof -t -i:$port)

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