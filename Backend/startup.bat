@echo off
set SBT_OPTS="-Xss1M"
set JVM_OPTS="-Xms256m -Xmx2048m -server"
sbt ;clean;run
