#!/bin/bash
: ${SBPM_PORT:=8080}
: ${AKKA_PORT:=2552}

nohup java -jar Backend/backend_$SBPM_PORT.jar > log/backend_$SBPM_PORT.log 2>&1 < /dev/null &
echo $! > pids/backend_$!.pid
