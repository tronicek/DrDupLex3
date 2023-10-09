@echo off
call ..\setEnv.bat

set SRC_DIR="/research/h2/h2database-version-1.4.196/h2/src/main"

java %OPTIONS% -jar %DRDUPLEX_JAR% h2.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drdup-h2.xml
java %OPTIONS% -jar %CHECKER_JAR% checker.properties
