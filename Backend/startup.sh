bin/sbt.sh clean
JVM_OPTS="-Xms256m -Xmx2048m -server" SBT_OPTS="-Xss1M" sbt run
