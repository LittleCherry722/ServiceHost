#!/bin/bash
: ${SBPM_PORT:=8080}
: ${AKKA_PORT:=2552}
echo "Hostname:"
echo $AKKA_HOSTNAME
nohup java -jar backend.jar > log/backend_$SBPM_PORT.log 2>&1 < /dev/null &
echo $! > pids/backend_$!.pid
