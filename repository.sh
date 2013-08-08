#!/bin/bash

nohup java -jar repository.jar > /dev/null 2>&1 < /dev/null
echo $! > pids/repository.pid
