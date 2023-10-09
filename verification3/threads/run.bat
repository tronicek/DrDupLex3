@echo off
call ..\setEnv.bat

java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-1.properties
java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-2.properties
java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-4.properties
java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-8.properties
java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-16.properties
java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-32.properties
java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-64.properties
java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-128.properties
java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-256.properties

java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-1.xml jedit-2.xml diff-1-2.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-2.xml jedit-4.xml diff-2-4.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-4.xml jedit-8.xml diff-4-8.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-8.xml jedit-16.xml diff-8-16.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-16.xml jedit-32.xml diff-16-32.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-32.xml jedit-64.xml diff-32-64.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-64.xml jedit-128.xml diff-64-128.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-128.xml jedit-256.xml diff-128-256.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-256.xml jedit-1.xml diff-256-1.xml
