@echo off
set SRC_DIR="/research/BigCloneEval/BigCloneEval/ijadataset/bcb_reduced"
set DRDUPLEX_JAR="/research/projects/DrDupLex3/target/DrDupLex3-1.0-jar-with-dependencies.jar"
set TOOL_JAR="/research/projects/EvalTool/target/EvalTool-1.0-jar-with-dependencies.jar"
set CHECKER_JAR="/research/projects/CloneChecker/target/CloneChecker-1.0-jar-with-dependencies.jar"
set DISTANCE_JAR="/research/projects/CloneDistance/target/CloneDistance-1.0-jar-with-dependencies.jar"
set OPTIONS=-ea -Xmx10G

REM DrDupLex3
java %OPTIONS% -jar %DRDUPLEX_JAR% bigclonebench-15.properties
java %OPTIONS% -jar %DRDUPLEX_JAR% bigclonebench-40.properties

java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drduplex-bigclonebench-40.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drduplex-bigclonebench-40-separated.xml
java %OPTIONS% -jar %CHECKER_JAR% drduplex-bigclonebench-40-checker.properties

REM NiCad
set BIGCLONEBENCH_SRC_DIR="/home/TRONZ635/research/BigCloneBench/"
java -cp %TOOL_JAR% drdup.ChangeDir %BIGCLONEBENCH_SRC_DIR% nicad-0.20-10.xml
java -cp %TOOL_JAR% drdup.ChangeDir %BIGCLONEBENCH_SRC_DIR% nicad-0.10-20.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% nicad-0.10-20-dir.xml
java %OPTIONS% -cp %DISTANCE_JAR% nicad.EditDistance nicad-distance.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.FilterDistance 0 1 nicad-0.10-20-dir-source-distance.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Diff drduplex-bigclonebench-40-separated.xml nicad-0.20-10-dir.xml drduplex-nicad.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff nicad-0.10-20-dir-source-distance-filtered.xml drduplex-bigclonebench-15.xml nicad-drduplex.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drduplex-nicad.xml

java %OPTIONS% -jar %CHECKER_JAR% drduplex-nicad-checker.properties
java %OPTIONS% -jar %CHECKER_JAR% nicad-drduplex-checker.properties

java %OPTIONS% -cp %TOOL_JAR% drdup.Counter drduplex-nicad.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Counter nicad-drduplex.xml

REM CloneWorks
set BIGCLONEBENCH_SRC_DIR="/home/TRONZ635/research/BigCloneBench/"
java -cp %TOOL_JAR% drdup.ChangeDir %BIGCLONEBENCH_SRC_DIR% cloneworks-0.90-20.xml
java -cp %TOOL_JAR% drdup.ChangeDir %BIGCLONEBENCH_SRC_DIR% cloneworks-0.90-10.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% cloneworks-0.90-20-dir.xml
java %OPTIONS% -cp %DISTANCE_JAR% nicad.EditDistance cloneworks-distance.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.FilterDistance 0 1 cloneworks-0.90-20-dir-source-distance.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Diff drduplex-bigclonebench-40-separated.xml cloneworks-0.90-10-dir.xml drduplex-cloneworks.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff cloneworks-0.90-20-dir-source-distance-filtered.xml drduplex-bigclonebench-15.xml cloneworks-drduplex.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drduplex-cloneworks.xml

java %OPTIONS% -jar %CHECKER_JAR% drduplex-cloneworks-checker.properties
java %OPTIONS% -jar %CHECKER_JAR% cloneworks-drduplex-checker.properties

java %OPTIONS% -cp %TOOL_JAR% drdup.Counter drduplex-cloneworks.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Counter cloneworks-drduplex.xml
