nohup java -jar backend.jar > /dev/null 2>&1 &
echo $! > pids/backend.pid
