@echo off
call ..\setEnv.bat

set SRC_DIR="/research/OpenJDK/jaxp/src"

java %OPTIONS% -jar %DRDUPLEX_JAR% jaxp.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drdup-jaxp.xml
java %OPTIONS% -jar %CHECKER_JAR% checker.properties
