package edu.tarleton.drduplex.index.plain.persistent;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import edu.tarleton.drduplex.Engine;
import edu.tarleton.drduplex.Histogram;
import edu.tarleton.drduplex.NormalizingVisitor;
import edu.tarleton.drduplex.index.IndexBuilder;
import edu.tarleton.drduplex.index.plain.SimplifiedLexIndexBuilder;
import edu.tarleton.drduplex.index.plain.SimplifiedLexIndexStmtBuilder;
import edu.tarleton.drduplex.index.plain.Trie;
import edu.tarleton.drduplex.nicad.NiCadConvertor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * The class that builds the plain (not compressed) index and finds the clones.
 *
 * @author Zdenek Tronicek
 */
public class PlainPersistentEngine extends Engine {

    public static final int DEFAULT_NODE_FILE_PAGE_SIZE = PNode.LENGTH * 1024 * 1024 * 60;
    public static final int DEFAULT_EDGE_FILE_PAGE_SIZE = PEdgeBlock.LENGTH * 1024 * 1024 * 4;
    public static final int DEFAULT_POS_FILE_PAGE_SIZE = PPos.LENGTH * 1024 * 1024 * 40;
    private final String nodeFileName;
    private final int nodeFilePageSize;
    private final String edgeFileName;
    private final int edgeFilePageSize;
    private final String posFileName;
    private final int posFilePageSize;
    private final String pathFileName;
    private final String labelFileName;
    private final String nextStmtMapFileName;
    private final int batchFileSize;

    public PlainPersistentEngine(Properties conf) {
        super(conf);
        nodeFileName = conf.getProperty("nodeFile");
        nodeFilePageSize = getIntProperty(conf, "nodeFilePageSize", DEFAULT_NODE_FILE_PAGE_SIZE);
        edgeFileName = conf.getProperty("edgeFile");
        edgeFilePageSize = getIntProperty(conf, "edgeFilePageSize", DEFAULT_EDGE_FILE_PAGE_SIZE);
        posFileName = conf.getProperty("posFile");
        posFilePageSize = getIntProperty(conf, "posFilePageSize", DEFAULT_POS_FILE_PAGE_SIZE);
        pathFileName = conf.getProperty("pathFile");
        labelFileName = conf.getProperty("labelFile");
        nextStmtMapFileName = conf.getProperty("nextStmtMapFile");
        batchFileSize = Integer.parseInt(conf.getProperty("batchFileSize", "1000"));
    }

    private int getIntProperty(Properties conf, String name, int defaultValue) {
        String s = conf.getProperty(name);
        if (s == null) {
            return defaultValue;
        }
        return Integer.parseInt(s);
    }

    @Override
    public void findClones() throws Exception {
        boolean methodLevel = level.equals("method");
        Path dir = Paths.get(sourceDir).toAbsolutePath();
        IndexBuilder builder;
        switch (index) {
            case "simplified":
                builder = methodLevel
                        ? new SimplifiedLexIndexBuilder(conf, dir)
                        : new SimplifiedLexIndexStmtBuilder(conf, dir);
                break;
            default:
                throw new AssertionError("invalid index: " + index);
        }
        try (PTrie trie = PTrie.initialize(nodeFileName, nodeFilePageSize,
                edgeFileName, edgeFilePageSize,
                posFileName, posFilePageSize,
                pathFileName, labelFileName,
                nextStmtMapFileName)) {
            processDir(builder, sourceDir, trie);
            if (fileCount > 0) {
                Trie t = builder.getTrie();
                trie.addTrie(t);
                if (printStatistics) {
                    statistics.store(countingVisitor.getLines(), countingVisitor.getNodes(), PNode.getCount(), PEdge.getCount(), PPos.getCount());
                }
            }
            if (printStatistics) {
                statistics.print(true);
            }
            if (printTrie) {
                trie.print();
            }
            if (printHistogram) {
                Histogram hist = trie.createHistogram();
                hist.print();
            }
            if (verbose) {
                System.out.println("searching for clones...");
            }
            switch (type) {
                case "2":
                    cloneSet = trie.detectClonesType2(level, minSize, maxSize);
                    break;
                case "3":
                    cloneSet = trie.detectClonesType3(level, threads, maxDistance, minSize, maxSize);
                    break;
                case "2+3":
                    cloneSet = trie.detectClonesType23(level, threads, maxDistance, minSize, maxSize);
                    break;
                default:
                    throw new AssertionError("invalid type: " + type);
            }
            if (outputFileName == null) {
                cloneSet.print();
            } else {
                NiCadConvertor conv = new NiCadConvertor(conf);
                conv.convert(cloneSet, outputFileName);
            }
        }
    }

    void processDir(IndexBuilder builder, String srcDir, PTrie trie) throws IOException {
        Path path = Paths.get(srcDir);
        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        processFile(builder, srcDir, p, trie);
                    });
        }
    }

    private void processFile(IndexBuilder builder, String srcDir, Path path, PTrie trie) {
        String fn = path.toString().substring(srcDir.length());
        if (fn.startsWith("/") || fn.startsWith("\\")) {
            fn = fn.substring(1);
        }
        if (verbose) {
            System.out.printf("processing %s...%n", fn);
        }
        NormalizingVisitor normVisitor = new NormalizingVisitor(conf);
        Path root = Paths.get(srcDir);
        SourceRoot sourceRoot = new SourceRoot(root, parserConfiguration);
        try {
            CompilationUnit cu = sourceRoot.parse("", fn);
            if (printStatistics) {
                cu.accept(countingVisitor, null);
            }
            cu.accept(normVisitor, null);
            cu.accept(builder, null);
            fileCount++;
            if (fileCount == batchFileSize) {
                Trie t = builder.getTrie();
                trie.addTrie(t);
                if (printStatistics) {
                    statistics.store(countingVisitor.getLines(),
                            countingVisitor.getNodes(), PNode.getCount(),
                            PEdge.getCount(), PPos.getCount());
                }
                builder.reset();
                fileCount = 0;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void printTrie() throws Exception {
        try (PTrie trie = PTrie.fromFiles(nodeFileName, nodeFilePageSize,
                edgeFileName, edgeFilePageSize,
                posFileName, posFilePageSize,
                pathFileName, labelFileName,
                nextStmtMapFileName)) {
            trie.print();
        }
    }
}
