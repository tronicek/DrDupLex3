@echo off
set SRC_DIR="/research/BigCloneEval/BigCloneEval/ijadataset/bcb_reduced"
set DRDUPLEX3_JAR="/research/projects/DrDupLex3/target/DrDupLex3-1.0-jar-with-dependencies.jar"
set TOOL_JAR="/research/projects/EvalTool/target/EvalTool-1.0-jar-with-dependencies.jar"
set CHECKER_JAR="/research/projects/CloneChecker/target/CloneChecker-1.0-jar-with-dependencies.jar"
set DISTANCE_JAR="/research/projects/NiCadDistance/target/NiCadDistance-1.0-jar-with-dependencies.jar"
set OPTIONS=-ea -Xmx10G

REM java %OPTIONS% -jar %DRDUPLEX3_JAR% bigclonebench-1.properties
REM java %OPTIONS% -jar %DRDUPLEX3_JAR% bigclonebench-8.properties

set BIGCLONEBENCH_SRC_DIR="systems/bcb_reduced/"
java -cp %TOOL_JAR% drdup.ChangeDir %BIGCLONEBENCH_SRC_DIR% nicad-blind-clones-0.05-20.xml
java -cp %TOOL_JAR% drdup.ChangeDir %BIGCLONEBENCH_SRC_DIR% nicad-blind-clones-0.10-10.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drduplex-bigclonebench-1.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% nicad-blind-clones-0.05-20-dir.xml
java %OPTIONS% -cp %DISTANCE_JAR% nicad.Main nicad-distance.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.FilterLines 15 1000000 nicad-blind-clones-0.05-20-dir-source-distance.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.FilterDistance 0 4 nicad-blind-clones-0.05-20-dir-source-distance-filtered.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Diff drduplex-bigclonebench-1-separated.xml nicad-blind-clones-0.10-10-dir.xml drduplex-nicad.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff nicad-blind-clones-0.05-20-dir-source-distance-filtered-filtered.xml drduplex-bigclonebench-4.xml nicad-drduplex.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drduplex-bigclonebench-1-separated.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drduplex-nicad.xml

java %OPTIONS% -jar %CHECKER_JAR% drduplex-checker.properties
java %OPTIONS% -jar %CHECKER_JAR% drduplex-nicad-checker.properties
java %OPTIONS% -jar %CHECKER_JAR% nicad-drduplex-checker.properties

java %OPTIONS% -cp %TOOL_JAR% drdup.Counter drduplex-nicad.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Counter nicad-drduplex.xml

set BIGCLONEBENCH_SRC_DIR="example/bcb_reduced/"
java -cp %TOOL_JAR% drdup.ChangeDir %BIGCLONEBENCH_SRC_DIR% cloneworks-0.95-20.xml
java -cp %TOOL_JAR% drdup.ChangeDir %BIGCLONEBENCH_SRC_DIR% cloneworks-0.90-10.xml

java -cp %TOOL_JAR% drdup.FilterInvalidFile %SRC_DIR% cloneworks-0.95-20-dir.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% cloneworks-0.95-20-dir-filtered.xml
java %OPTIONS% -cp %DISTANCE_JAR% nicad.Main cloneworks-distance.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.FilterLines 15 1000000 cloneworks-0.95-20-dir-filtered-source-distance.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.FilterDistance 0 4 cloneworks-0.95-20-dir-filtered-source-distance-filtered.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Diff drduplex-bigclonebench-1-separated.xml cloneworks-0.90-10-dir.xml drduplex-cloneworks.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff cloneworks-0.95-20-dir-filtered-source-distance-filtered-filtered.xml drduplex-bigclonebench-4.xml cloneworks-drduplex.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drduplex-cloneworks.xml

java %OPTIONS% -jar %CHECKER_JAR% drduplex-cloneworks-checker.properties
java %OPTIONS% -jar %CHECKER_JAR% cloneworks-drduplex-checker.properties

java %OPTIONS% -cp %TOOL_JAR% drdup.Counter drduplex-cloneworks.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Counter cloneworks-drduplex.xml
