#!/usr/bin/env bash

OS=$(uname -s)
ARCH=$(uname -m)

if [ "$OS" = "Darwin" ]; then
  PLATFORM="macos"
elif [ "$OS" = "Linux" ]; then
  PLATFORM="linux"
else
  echo "Unsupported operating system: $OS"
  exit 1
fi

if [ "$ARCH" = "arm64" ] || [ "$ARCH" = "aarch64" ]; then
  ARCH_TYPE="arm64"
elif [ "$ARCH" = "amd64" ] || [ "$ARCH" = "x86_64" ]; then
  ARCH_TYPE="amd64"
else
  echo "Unsupported architecture: $ARCH"
  exit 1
fi

X2I_URL="https://github.com/perfana/x2i/releases/download/x2i-1.0.0/x2i-${PLATFORM}-${ARCH_TYPE}"
echo "Downloading x2i from $X2I_URL"
if wget -q $X2I_URL -O x2i; then
  echo "Successfully downloaded x2i."
else
  echo "Failed to download x2i."
  exit 1
fi
chmod u+x x2i

JFR_EXPORTER_URL="https://github.com/perfana/jfr-exporter/releases/download/0.5.0/jfr-exporter-0.5.0.jar"
echo "Downloading jfr-exporter.jar from $JFR_EXPORTER_URL"
if wget -q $JFR_EXPORTER_URL -O jfr-exporter.jar; then
  echo "Successfully downloaded jfr-exporter.jar."
else
  echo "Failed to download jfr-exporter.jar."
  exit 1
fi

OTEL_AGENT_URL="https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar"
echo "Downloading opentelemetry-javaagent.jar from $OTEL_AGENT_URL"
if wget -q $OTEL_AGENT_URL -O opentelemetry-javaagent.jar; then
  echo "Successfully downloaded opentelemetry-javaagent.jar."
else
  echo "Failed to download opentelemetry-javaagent.jar."
  exit 1
fi

