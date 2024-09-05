SRC_DIR=/home/TRONZ635/research/BigCloneBench/
DRDUPLEX_JAR=../DrDupLex3-1.0-jar-with-dependencies.jar
TOOL_JAR=../EvalTool-1.0-jar-with-dependencies.jar
CHECKER_JAR=../CloneChecker-1.0-jar-with-dependencies.jar
DISTANCE_JAR=../CloneDistance-1.0-jar-with-dependencies.jar
OPTIONS=-mx10G

# DrDupLex3
java $OPTIONS -jar $DRDUPLEX_JAR bigclonebench-6.properties
java $OPTIONS -jar $DRDUPLEX_JAR bigclonebench-40.properties

java $OPTIONS -cp $TOOL_JAR drdup.Separator drduplex-bigclonebench-40.xml
java $OPTIONS -cp $TOOL_JAR drdup.Sourcer $SRC_DIR drduplex-bigclonebench-40-separated.xml
java $OPTIONS -jar $CHECKER_JAR drduplex-bigclonebench-40-checker.properties

# NiCad
java $OPTIONS -cp $TOOL_JAR drdup.ChangeDir $SRC_DIR nicad-0.20-10.xml
java $OPTIONS -cp $TOOL_JAR drdup.ChangeDir $SRC_DIR nicad-0.10-20.xml

java $OPTIONS -cp $TOOL_JAR drdup.Sourcer $SRC_DIR nicad-0.10-20-dir.xml
java $OPTIONS -cp $DISTANCE_JAR nicad.EditDistance nicad-distance.properties
java $OPTIONS -cp $TOOL_JAR drdup.FilterDistance 0 1 nicad-0.10-20-dir-source-distance.xml

java $OPTIONS -cp $TOOL_JAR drdup.Diff drduplex-bigclonebench-40-separated.xml nicad-0.20-10-dir.xml drduplex-nicad.xml
java $OPTIONS -cp $TOOL_JAR drdup.Diff nicad-0.10-20-dir-source-distance-filtered.xml drduplex-bigclonebench-6.xml nicad-drduplex.xml

java $OPTIONS -cp $TOOL_JAR drdup.Sourcer $SRC_DIR drduplex-nicad.xml

java $OPTIONS -jar $CHECKER_JAR drduplex-nicad-checker.properties
java $OPTIONS -jar $CHECKER_JAR nicad-drduplex-checker.properties

java $OPTIONS -cp $TOOL_JAR drdup.Counter drduplex-nicad.xml
java $OPTIONS -cp $TOOL_JAR drdup.Counter nicad-drduplex.xml

# CloneWorks
java $OPTIONS -cp $TOOL_JAR drdup.ChangeDir $SRC_DIR cloneworks-0.90-20.xml
java $OPTIONS -cp $TOOL_JAR drdup.ChangeDir $SRC_DIR cloneworks-0.90-10.xml

java $OPTIONS -cp $TOOL_JAR drdup.Sourcer $SRC_DIR cloneworks-0.90-20-dir.xml
java $OPTIONS -cp $DISTANCE_JAR nicad.EditDistance cloneworks-distance.properties
java $OPTIONS -cp $TOOL_JAR drdup.FilterDistance 0 1 cloneworks-0.90-20-dir-source-distance.xml

java $OPTIONS -cp $TOOL_JAR drdup.Diff cloneworks-0.90-20-dir-source-distance-filtered.xml drduplex-bigclonebench-6.xml cloneworks-drduplex.xml
java $OPTIONS -cp $TOOL_JAR drdup.Diff drduplex-bigclonebench-40-separated.xml cloneworks-0.90-10-dir.xml drduplex-cloneworks.xml

java $OPTIONS -cp $TOOL_JAR drdup.Sourcer $SRC_DIR drduplex-cloneworks.xml

java $OPTIONS -jar $CHECKER_JAR cloneworks-drduplex-checker.properties
java $OPTIONS -jar $CHECKER_JAR drduplex-cloneworks-checker.properties

java $OPTIONS -cp $TOOL_JAR drdup.Counter drduplex-cloneworks.xml
java $OPTIONS -cp $TOOL_JAR drdup.Counter cloneworks-drduplex.xml

# SourcererCC
java $OPTIONS -cp $TOOL_JAR drdup.FilterLines 20 10000 sourcerercc-0.90.xml
java $OPTIONS -cp $TOOL_JAR drdup.Sourcer $SRC_DIR sourcerercc-0.90-filtered.xml
java $OPTIONS -cp $DISTANCE_JAR nicad.EditDistance sourcerercc-distance-0.90.properties
java $OPTIONS -cp $TOOL_JAR drdup.FilterDistance 0 1 sourcerercc-0.90-filtered-source-distance.xml
java $OPTIONS -cp $TOOL_JAR drdup.Diff sourcerercc-0.90-filtered-source-distance-filtered.xml drduplex-bigclonebench-6.xml sourcerercc-drduplex.xml

java $OPTIONS -cp $TOOL_JAR drdup.Diff drduplex-bigclonebench-40-separated.xml sourcerercc-0.80.xml drduplex-sourcerercc.xml

java $OPTIONS -cp $TOOL_JAR drdup.Sourcer $SRC_DIR drduplex-sourcerercc.xml

java $OPTIONS -jar $CHECKER_JAR drduplex-sourcerercc-checker.properties
java $OPTIONS -jar $CHECKER_JAR sourcerercc-drduplex-checker.properties

java $OPTIONS -cp $TOOL_JAR drdup.Counter drduplex-sourcerercc.xml
java $OPTIONS -cp $TOOL_JAR drdup.Counter sourcerercc-drduplex.xml

