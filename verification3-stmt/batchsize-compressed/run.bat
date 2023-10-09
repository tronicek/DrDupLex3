@echo off
call ..\setEnv.bat

java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-1.properties
java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-5.properties
java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-10.properties
java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-50.properties
java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-100.properties
java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-500.properties
java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-1000.properties
java %OPTIONS% -jar %DRDUPLEX_JAR% jedit-5000.properties

java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-1.xml jedit-5.xml diff-1-5.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-5.xml jedit-10.xml diff-5-10.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-10.xml jedit-50.xml diff-10-50.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-50.xml jedit-100.xml diff-50-100.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-100.xml jedit-500.xml diff-100-500.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-500.xml jedit-1000.xml diff-500-1000.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-1000.xml jedit-5000.xml diff-1000-5000.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff jedit-5000.xml jedit-1.xml diff-5000-1.xml

del data\*.bin
