@echo off
call ..\setEnv.bat

set SRC_DIR="/research/mockito/mockito-2.2.0/src/main"

java %OPTIONS% -jar %DRDUPLEX_JAR% mockito.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drdup-mockito.xml
java %OPTIONS% -jar %CHECKER_JAR% checker.properties
