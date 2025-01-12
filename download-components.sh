#!/usr/bin/env bash

wget https://github.com/perfana/x2i/releases/download/x2i-1.0.0/x2i-macos-arm64
mv x2i-macos-arm64 x2i
chmod u+x x2i

wget https://github.com/perfana/jfr-exporter/releases/download/0.5.0/jfr-exporter-0.5.0.jar
mv jfr-exporter-0.5.0.jar jfr-exporter.jar
