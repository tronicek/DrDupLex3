package edu.tarleton.drduplex.index;

import edu.tarleton.drduplex.clones.Pos;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The representation of a lexical unit (method or statement).
 *
 * @author Zdenek Tronicek
 */
public class Unit {

    private static int count;
    private final int id;
    private final Pos[] pos;
    private final int[] tokens;
    private transient boolean completed;
    private final List<Integer> nonClones = Collections.synchronizedList(new ArrayList<>());
    private final Map<Integer, Integer> distanceMap = Collections.synchronizedMap(new HashMap<>());

    public Unit(Pos[] pos, int[] tokens) {
        id = count;
        this.pos = pos;
        this.tokens = tokens;
        count++;
    }

    public int getId() {
        return id;
    }

    public Pos[] getPos() {
        return pos;
    }

    public int[] getTokens() {
        return tokens;
    }

    public void completed() {
        completed = true;
        nonClones.clear();
    }

    public void clear() {
        distanceMap.clear();
    }

    public Map<Integer, Integer> getDistanceMap() {
        return distanceMap;
    }

    public int getSize() {
        return tokens.length;
    }

    public void computeDistanceTo(int maxDistance, Unit unit) {
        if (completed
                || unit.completed
                || nonClones.contains(unit.id)
                || unit.nonClones.contains(id)
                || distanceMap.containsKey(unit.id)
                || unit.distanceMap.containsKey(id)) {
            return;
        }
        if (unit == this) {
            return;
        }
        int dist = levenshteinDistance(unit.tokens, maxDistance + 1);
        if (dist <= maxDistance) {
            distanceMap.put(unit.id, dist);
        } else {
            nonClones.add(unit.id);
        }
    }

    private int levenshteinDistance(int[] tokens2, int infDistance) {
        int m = tokens.length;
        int[] d = new int[m + 1];
        for (int i = 0; i < d.length; i++) {
            d[i] = i;
        }
        int[] nd = new int[d.length];
        for (int i = 0; i < tokens2.length; i++) {
            nd[0] = i + 1;
            int dist = nd[0];
            for (int j = 0; j < tokens.length; j++) {
                if (tokens[j] == tokens2[i]) {
                    nd[j + 1] = d[j];
                } else {
                    nd[j + 1] = 1 + min(d[j + 1], nd[j], d[j]);
                }
                if (nd[j + 1] < dist) {
                    dist = nd[j + 1];
                }
            }
            if (dist >= infDistance) {
                return infDistance;
            }
            int[] p = d;
            d = nd;
            nd = p;
        }
        return d[m];
    }

    private int min(int a, int b, int c) {
        int m = a < b ? a : b;
        if (c < m) {
            m = c;
        }
        return m;
    }

    public void print() {
        System.out.printf("unit [id: %d, pos:", id);
        for (Pos p : pos) {
            System.out.printf(" %s", p);
        }
        System.out.println("]");
    }

    public void printDistanceMap() {
        print();
        System.out.print("  tokens:");
        for (int tok : tokens) {
            System.out.printf(" %d", tok);
        }
        System.out.printf("%n  distance map:");
        for (Integer mid : distanceMap.keySet()) {
            Integer dist = distanceMap.get(mid);
            System.out.printf(" %d:%d", mid, dist);
        }
        System.out.println();
    }
}
