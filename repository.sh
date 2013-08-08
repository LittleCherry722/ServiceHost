#!/bin/bash

nohup java -jar repository.jar > log/repository.log 2>&1 < /dev/null &
echo $! > pids/repository_$1.pid
