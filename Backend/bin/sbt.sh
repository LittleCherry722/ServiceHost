java -Xms256M -Xmx768M -Xss1M $SBT_OPTS -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=384M -jar `dirname $0`/sbt-launch.jar "$@"
