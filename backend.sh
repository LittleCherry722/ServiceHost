#!/bin/sh

nohup java -jar backend.jar > /dev/null 2>&1 < /dev/null &
echo $! > pids/backend.pid
