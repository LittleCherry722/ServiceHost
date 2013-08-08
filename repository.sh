nohup java -jar repository.jar > /dev/null 2>&1 &
echo $! > pids/repository.pid
