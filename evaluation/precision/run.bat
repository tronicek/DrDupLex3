@echo off
set SRC_DIR="/research/BigCloneEval/BigCloneEval/ijadataset/bcb_reduced"
set DRDUPLEX_JAR="/research/projects/DrDupLex3/target/DrDupLex3-1.0-jar-with-dependencies.jar"
set TOOL_JAR="/research/projects/EvalTool/target/EvalTool-1.0-jar-with-dependencies.jar"
set CHECKER_JAR="/research/projects/CloneChecker/target/CloneChecker-1.0-jar-with-dependencies.jar"
set DISTANCE_JAR="/research/projects/CloneDistance/target/CloneDistance-1.0-jar-with-dependencies.jar"
set OPTIONS=-ea -Xmx10G

REM NiCad
set BIGCLONEBENCH_SRC_DIR="/home/TRONZ635/research/BigCloneBench/"
java -cp %TOOL_JAR% drdup.ChangeDir %BIGCLONEBENCH_SRC_DIR% nicad-0.01-20.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% nicad-0.01-20-dir.xml
java %OPTIONS% -cp %DISTANCE_JAR% nicad.EditDistance nicad-distance23.properties
java %OPTIONS% -jar %CHECKER_JAR% nicad-checker23.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Counter nicad-0.01-20-dir.xml

java -cp %TOOL_JAR% drdup.ChangeDir %BIGCLONEBENCH_SRC_DIR% nicad-0.05-20.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.FilterSimilarity 1 99 nicad-0.05-20-dir.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% nicad-0.05-20-dir-filtered.xml
java %OPTIONS% -cp %DISTANCE_JAR% nicad.EditDistance nicad-distance3.properties
java %OPTIONS% -jar %CHECKER_JAR% nicad-checker3.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Counter nicad-0.05-20-dir-filtered.xml

REM CloneWorks
set BIGCLONEBENCH_SRC_DIR="/home/TRONZ635/research/BigCloneBench/"
java -cp %TOOL_JAR% drdup.ChangeDir %BIGCLONEBENCH_SRC_DIR% cloneworks-0.99-20.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% cloneworks-0.99-20-dir.xml
java %OPTIONS% -cp %DISTANCE_JAR% nicad.EditDistance cloneworks-distance23.properties
java %OPTIONS% -jar %CHECKER_JAR% cloneworks-checker23.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Counter cloneworks-0.99-20-dir.xml

REM DrDupLex3
java %OPTIONS% -jar %DRDUPLEX_JAR% bigclonebench23.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drduplex-bigclonebench23.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drduplex-bigclonebench23-separated.xml
java %OPTIONS% -jar %CHECKER_JAR% drduplex-checker23.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Counter drduplex-bigclonebench23-separated.xml

java %OPTIONS% -jar %DRDUPLEX_JAR% bigclonebench3.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drduplex-bigclonebench3.xml
java %OPTIONS% -jar %CHECKER_JAR% drduplex-checker3.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Counter drduplex-bigclonebench3.xml
