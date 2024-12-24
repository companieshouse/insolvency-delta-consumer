#!/bin/bash
#
# Start script for insolvency-delta-consumer


PORT=8080
exec java -jar -Dserver.port="${PORT}" "insolvency-delta-consumer.jar"
