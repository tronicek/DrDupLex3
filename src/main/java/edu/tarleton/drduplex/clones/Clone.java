package edu.tarleton.drduplex.clones;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The representation of a clone.
 *
 * @author Zdenek Tronicek
 */
public class Clone {

    private final Integer distance;
    private final Pos[] positions;

    public Clone(Integer distance, Pos[] positions) {
        this.distance = distance;
        this.positions = positions;
    }

    public Integer getDistance() {
        return distance;
    }

    public Pos[] getPositions() {
        return positions;
    }

    public boolean subsetOf(Clone that) {
        for (Pos pos : positions) {
            if (!subsetOf(pos, that.positions)) {
                return false;
            }
        }
        return true;
    }

    private boolean subsetOf(Pos pos, Pos[] positions) {
        for (Pos p : positions) {
            if (pos.subsetOf(p)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(distance);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Clone) {
            Clone that = (Clone) obj;
            return distance.equals(that.distance)
                    && equal(positions, that.positions);
        }
        return false;
    }

    private boolean equal(Pos[] pp1, Pos[] pp2) {
        List<Pos> list1 = Arrays.asList(pp1);
        List<Pos> list2 = Arrays.asList(pp2);
        return list1.containsAll(list2) && list2.containsAll(list1);
    }
}
