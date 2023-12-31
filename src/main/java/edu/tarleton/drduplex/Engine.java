package edu.tarleton.drduplex;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import edu.tarleton.drduplex.clones.CloneSet;
import edu.tarleton.drduplex.index.compressed.MemoryCompressedEngine;
import edu.tarleton.drduplex.index.compressed.persistent.CPEngine;
import edu.tarleton.drduplex.index.plain.MemoryPlainEngine;
import edu.tarleton.drduplex.index.plain.persistent.PlainPersistentEngine;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * The class that builds the index and finds the clones.
 *
 * @author Zdenek Tronicek
 */
public abstract class Engine {

    protected final Properties conf;
    protected final String index;
    protected final String level;
    protected final String type;
    protected final String sourceDir;
    protected final String outputFileName;
    protected final int minSize;
    protected final int maxSize;
    protected final int maxDistance;
    protected final int threads;
    protected final boolean printHistogram;
    protected final boolean printStatistics;
    protected final boolean printTrie;
    protected final boolean verbose;
    protected final boolean preprocessUnicodeEscapes;
    protected final String languageLevel;
    protected final String sourceEncoding;
    protected final ParserConfiguration parserConfiguration = new ParserConfiguration();
    protected final CountingVisitor countingVisitor;
    protected final Statistics statistics;
    protected int fileCount;
    protected CloneSet cloneSet;

    protected Engine(Properties conf) {
        this.conf = conf;
        index = conf.getProperty("index", "simplified");
        level = conf.getProperty("level", "method");
        type = conf.getProperty("type", "2");
        sourceDir = conf.getProperty("sourceDir");
        outputFileName = conf.getProperty("outputFile");
        minSize = Integer.parseInt(conf.getProperty("minSize", "0"));
        maxSize = Integer.parseInt(conf.getProperty("maxSize", "1000000"));
        switch (type) {
            case "2+3":
            case "3":
                maxDistance = Integer.parseInt(conf.getProperty("maxDistance"));
                break;
            default:
                maxDistance = 0;
        }
        threads = Integer.parseInt(conf.getProperty("threads", "1"));
        printHistogram = Boolean.parseBoolean(conf.getProperty("printHistogram"));
        printStatistics = Boolean.parseBoolean(conf.getProperty("printStatistics"));
        printTrie = Boolean.parseBoolean(conf.getProperty("printTrie"));
        verbose = Boolean.parseBoolean(conf.getProperty("verbose"));
        preprocessUnicodeEscapes = Boolean.parseBoolean(conf.getProperty("preprocessUnicodeEscapes"));
        languageLevel = conf.getProperty("languageLevel", "JAVA_8");
        sourceEncoding = conf.getProperty("sourceEncoding", "UTF-8");
        prepareParserConfiguration();
        countingVisitor = printStatistics ? new CountingVisitor() : null;
        statistics = printStatistics ? new Statistics() : null;
    }

    private void prepareParserConfiguration() {
        LanguageLevel lang = LanguageLevel.valueOf(languageLevel);
        Charset cs = Charset.forName(sourceEncoding);
        parserConfiguration.setPreprocessUnicodeEscapes(preprocessUnicodeEscapes);
        parserConfiguration.setLanguageLevel(lang);
        parserConfiguration.setCharacterEncoding(cs);
    }

    public static Engine instance(Properties conf) {
        boolean compressed = Boolean.parseBoolean(conf.getProperty("compressed"));
        boolean persistent = Boolean.parseBoolean(conf.getProperty("persistent"));
        if (persistent) {
            return compressed ? new CPEngine(conf) : new PlainPersistentEngine(conf);
        }
        return compressed ? new MemoryCompressedEngine(conf) : new MemoryPlainEngine(conf);
    }

    public CloneSet getCloneSet() {
        return cloneSet;
    }

    public abstract void findClones() throws Exception;

    public abstract void printTrie() throws Exception;
}
