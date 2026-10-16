@echo off
set MVN_CMD="%USERPROFILE%\apache-maven-3.9.9\bin\mvn.cmd"
if exist %MVN_CMD% (
    %MVN_CMD% %*
) else (
    mvn %*
)
