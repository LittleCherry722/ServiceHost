#!/bin/bash

nohup java -jar backend.jar > log/backend.log 2>&1 < /dev/null &
echo $! > pids/backend_$!.pid
