package edu.tarleton.drduplex.clones;

import java.util.ArrayList;
import java.util.List;

/**
 * The representation of a clone set.
 *
 * @author Zdenek Tronicek
 */
public class CloneSet {

    private final List<Clone> clones = new ArrayList<>();

    public List<Clone> getClones() {
        return clones;
    }

    public void addClone(Clone clone) {
        clones.add(clone);
    }

    public void addClones(List<Clone> cls) {
        clones.addAll(cls);
    }

    public void print() {
        for (Clone clone : clones) {
            System.out.printf("distance: %d, positions: %d%n", clone.getDistance(), clone.getPositions().length);
            for (Pos p : clone.getPositions()) {
                System.out.printf("  file: %s, start: %s, end: %s%n",
                        p.getFile(), p.getStart(), p.getEnd());
            }
        }
        int size = clones.size();
        System.out.printf("%d clone(s)%n", size);
    }
}
