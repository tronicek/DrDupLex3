package edu.tarleton.drduplex.index;

import edu.tarleton.drduplex.Histogram;
import edu.tarleton.drduplex.clones.CloneSet;

/**
 * The AST index.
 *
 * @author Zdenek Tronicek
 */
public interface PIndex {

    void print() throws Exception;

    CloneSet detectClonesType2(String level, int minSize, int maxSize) throws Exception;

    CloneSet detectClonesType3(String level, int threads, int maxDistance, int minSize, int maxSize) throws Exception;

    CloneSet detectClonesType23(String level, int threads, int maxDistance, int minSize, int maxSize) throws Exception;

    Histogram createHistogram() throws Exception;

}
