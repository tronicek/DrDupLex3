# DrDupLex3
DrDupLex3 is a novel Type-3 clone detector.

## Compilation
To compile the code, you need to install ```maven``` and run the following command (see also ```compile.bat```):
```
mvn clean compile assembly:single
```

## Running
The run and output of DrDupLex3 are controlled by a configuration file, which can contain the following configuration
parameters:
- *type* specifies the type of clones; accepted values are "2" (only Type-2 clones), "3" (only Type-3 clones that are not Type-2) and "2+3" (Type-2 and Type-3 clones).
- *maxDistance* specifies the maximum edit distance between two code fragments; for example, if *maxDistance* is 3, only the code fragments with an edit distance of 3 or less are reported.
- *level* specifies the granularity of the index; accepted values are "method" and "statement".
- *compressed* specifies whether the index is compressed or not; accepted values are "true" and "false".
- *persistent* specifies whether the index is built in main memory or on secondary storage; accepted values are "true" and "false".
- *minSize* specifies the minimum number of lines; for example, if *minSize* is 5, the code fragment must have at least 5 lines to be reported.
- *ignoreUnaryAtLiterals* specifies how the unary plus and minus are treated; accepted values are "true" and "false".
- *ignoreAnnotations* specifies whether annotations in code are taken into account; accepted values are "true" and "false".
- *treatNullAsLiteral* specifies how "null" is treated; accepted values are "true" and "false".
- *treatSuperThisAsIdentifier* specifies whether "super" and "this" are treated as identifiers; accepted values are "true" and "false".
- *threads* specifies the number of threads that are employed to compute edit distances between various code fragments.
- *batchFileSize* specifies how many files are processed before the index in memory is merged with the persistent index.

Example:
- index = simplified
- type = 2+3
- maxDistance = 2
- level = method
- rename = blind
- compressed = true
- persistent = false
- sourceDir = /research/BigCloneBench
- minSize = 20
- outputFile = drduplex-bigclonebench.xml

To run the clone detector with ```test.properties``` configuration file, use the following command:
```
java -jar target/DrDupLex3-1.0-jar-with-dependencies.jar test.properties
```

See also examples in the ```evaluation``` folder.
