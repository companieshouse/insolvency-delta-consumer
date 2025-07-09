#!/bin/bash
#
# Start script for insolvency-delta-consumer

PORT=8080
exec java -jar -Dserver.port="${PORT}" -XX:MaxRAMPercentage=80 "insolvency-delta-consumer.jar"
