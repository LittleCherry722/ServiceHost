@echo off
rem get new sbpm.log file name

set FOLDER=log
set FILENAME=%FOLDER%\sbpm.log
set NEWFILENAME=%FOLDER%\sbpm_%DATE:/=-%_%TIME::=-%.log


rem set sbt & jvm parameters
set SBT_OPTS="-Xss1M"
set JVM_OPTS="-Xms256m -Xmx2048m -server"

IF EXIST "%FOLDER%" (
  rem do nothing
) ELSE (
  mkdir "%FOLDER%"
)

IF EXIST "%FILENAME%" (
  move /Y "%FILENAME%" "%NEWFILENAME%" >nul
) ELSE (
  rem do nothing...
)

echo Starting server... (see %FILENAME% for further details)

rem and finally run sbpm in silent mode...
sbt ;clean;run > "%FILENAME%"

