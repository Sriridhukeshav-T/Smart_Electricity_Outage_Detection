@echo off
set MVN_CMD="%USERPROFILE%\apache-maven-3.9.9\bin\mvn.cmd"
if exist %MVN_CMD% (
    %MVN_CMD% -f backend/pom.xml %*
) else (
    mvn -f backend/pom.xml %*
)
