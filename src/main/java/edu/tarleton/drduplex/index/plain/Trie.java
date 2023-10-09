package edu.tarleton.drduplex.index.plain;

import edu.tarleton.drduplex.index.Task;
import edu.tarleton.drduplex.Histogram;
import edu.tarleton.drduplex.clones.Clone;
import edu.tarleton.drduplex.clones.CloneSet;
import edu.tarleton.drduplex.clones.Pos;
import edu.tarleton.drduplex.index.Index;
import edu.tarleton.drduplex.index.Unit;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The class that represents the TRIE.
 *
 * @author Zdenek Tronicek
 */
public class Trie implements Index, Serializable {

    private static final long serialVersionUID = 1L;
    public final TrieNode root = new TrieNode();
    private final Map<Pos, Pos> nextStmtMap = new HashMap<>();

    public TrieNode getRoot() {
        return root;
    }

    public Map<Pos, Pos> getNextStmtMap() {
        return nextStmtMap;
    }

    @Override
    public void nextStmt(Pos prev, Pos curr) {
        nextStmtMap.put(prev, curr);
    }

    @Override
    public void print() {
        root.print();
    }

    @Override
    public CloneSet detectClonesType2(String level, int minSize, int maxSize) {
        switch (level) {
            case "method":
            case "statement":
                return detectClonesType2(minSize, maxSize);
            case "statements":
                return detectMergedStmtClonesType2(minSize, maxSize);
            default:
                throw new AssertionError("invalid level: " + level);
        }
    }

    private CloneSet detectClonesType2(int minSize, int maxSize) {
        CloneSet clones = new CloneSet();
        List<TrieNode> nodes = new ArrayList<>();
        List<Pos[]> positions = new ArrayList<>();
        for (TrieEdge edge : root.getEdges()) {
            nodes.add(edge.getDestination());
            positions.add(edge.getPositions());
        }
        while (!nodes.isEmpty()) {
            TrieNode node = nodes.remove(0);
            Pos[] pp = positions.remove(0);
            Pos[] rr = positions(pp, minSize, maxSize);
            if (rr.length > 1) {
                Clone clone = new Clone(0, rr);
                clones.addClone(clone);
            }
            for (TrieEdge e : node.getEdges()) {
                nodes.add(e.getDestination());
                positions.add(e.getPositions());
            }
        }
        return clones;
    }

    private Pos[] positions(Pos[] positions, int minSize, int maxSize) {
        List<Pos> pp = new ArrayList<>();
        for (Pos p : positions) {
            long size = p.getLines();
            if (minSize <= size && size <= maxSize) {
                pp.add(p);
            }
        }
        Pos[] rr = new Pos[pp.size()];
        for (int i = 0; i < rr.length; i++) {
            rr[i] = pp.get(i);
        }
        return rr;
    }

    private CloneSet detectMergedStmtClonesType2(int minSize, int maxSize) {
        List<Clone> cls = new ArrayList<>();
        List<TrieNode> nodes = new ArrayList<>();
        List<Pos[]> positions = new ArrayList<>();
        for (TrieEdge edge : root.getEdges()) {
            nodes.add(edge.getDestination());
            positions.add(edge.getPositions());
        }
        while (!nodes.isEmpty()) {
            TrieNode node = nodes.remove(0);
            Pos[] pp = positions.remove(0);
            if (pp.length > 1) {
                Clone clone = new Clone(0, pp);
                cls.add(clone);
            }
            for (TrieEdge e : node.getEdges()) {
                nodes.add(e.getDestination());
                positions.add(e.getPositions());
            }
        }
        return merge(cls, minSize, maxSize);
    }

    private CloneSet merge(List<Clone> cls, int minSize, int maxSize) {
        List<Clone> cls2 = new ArrayList<>();
        Map<Pos, Set<Pos>> posMap = createPosMap(cls);
        for (Clone cl : cls) {
            Pos[] pp = cl.getPositions();
            Set<Integer> merged = new TreeSet<>();
            for (int i = 0; i < pp.length; i++) {
                if (merged.contains(i)) {
                    continue;
                }
                Pos next = nextStmtMap.get(pp[i]);
                Set<Pos> set = posMap.get(next);
                if (set == null) {
                    continue;
                }
                Set<Integer> inds = new TreeSet<>();
                for (int j = i + 1; j < pp.length; j++) {
                    Pos next2 = nextStmtMap.get(pp[j]);
                    if (set.contains(next2)) {
                        inds.add(j);
                    }
                }
                if (inds.isEmpty()) {
                    continue;
                }
                Pos[] np = new Pos[inds.size() + 1];
                np[0] = new Pos(pp[i].getFile(), pp[i].getStart(), next.getEnd());
                int k = 1;
                for (Integer j : inds) {
                    Pos next2 = nextStmtMap.get(pp[j]);
                    np[k] = new Pos(pp[j].getFile(), pp[j].getStart(), next2.getEnd());
                    k++;
                }
                Clone clone = new Clone(0, np);
                cls2.add(clone);
                merged.add(i);
                merged.addAll(inds);
            }
            if (merged.size() < pp.length) {
                cls2.add(cl);
            }
        }
        List<Clone> cls3 = new ArrayList<>();
        for (Clone cl : cls2) {
            Pos[] pp = cl.getPositions();
            Pos[] rr = positions(pp, minSize, maxSize);
            if (rr.length > 1) {
                Clone clone = new Clone(0, rr);
                cls3.add(clone);
            }
        }
        List<Clone> cls4 = removeDuplicates(cls3);
        CloneSet clones = new CloneSet();
        clones.addClones(cls4);
        return clones;
    }

    private Map<Pos, Set<Pos>> createPosMap(List<Clone> clones) {
        Map<Pos, Set<Pos>> map = new HashMap<>();
        for (Clone cl : clones) {
            for (Pos p : cl.getPositions()) {
                Set<Pos> pset = map.get(p);
                if (pset == null) {
                    pset = new HashSet<>();
                    map.put(p, pset);
                }
                for (Pos p2 : cl.getPositions()) {
                    if (p2 == p) {
                        continue;
                    }
                    pset.add(p2);
                }
            }
        }
        return map;
    }

    private List<Clone> removeDuplicates(List<Clone> clones) {
        Map<Clone, Integer> numMap = new HashMap<>();
        Map<Integer, Clone> revNumMap = new HashMap<>();
        for (int i = 0; i < clones.size(); i++) {
            Clone cl = clones.get(i);
            numMap.put(cl, i);
            revNumMap.put(i, cl);
        }
        Map<String, Set<Integer>> fileMap = new HashMap<>();
        for (Clone cl : clones) {
            Integer i = numMap.get(cl);
            for (Pos pos : cl.getPositions()) {
                String file = pos.getFile();
                Set<Integer> cls = fileMap.get(file);
                if (cls == null) {
                    cls = new HashSet<>();
                    fileMap.put(file, cls);
                }
                cls.add(i);
            }
        }
        List<Clone> subs = new ArrayList<>();
        for (Clone cl : clones) {
            Pos[] pp = cl.getPositions();
            String file = pp[0].getFile();
            Set<Integer> cls = new HashSet<>();
            Set<Integer> cc = fileMap.get(file);
            cls.addAll(cc);
            for (int i = 1; i < pp.length; i++) {
                String file2 = pp[i].getFile();
                Set<Integer> cls2 = fileMap.get(file2);
                cls.retainAll(cls2);
            }
            Integer i = numMap.get(cl);
            for (Integer j : cls) {
                if (i.equals(j)) {
                    continue;
                }
                Clone cl2 = revNumMap.get(j);
                if (cl.subsetOf(cl2)) {
                    subs.add(cl);
                    break;
                }
            }
        }
        List<Clone> clones2 = new ArrayList<>();
        for (Clone cl : clones) {
            if (!subs.contains(cl)) {
                clones2.add(cl);
            }
        }
        return clones2;
    }

    @Override
    public CloneSet detectClonesType3(String level, int threads, int maxDistance, int minSize, int maxSize) throws Exception {
        switch (level) {
            case "method":
            case "statement":
                return detectClonesType3(threads, maxDistance, minSize, maxSize);
            default:
                throw new AssertionError("invalid level: " + level);
        }
    }

    private CloneSet detectClonesType3(int threads, int maxDistance, int minSize, int maxSize) throws Exception {
        List<Unit> units = createUnitList(minSize, maxSize);
        root.nullEdges();
        Map<Integer, List<Unit>> map = createUnitSizeMap(units);
        Set<Integer> all = map.keySet();
        Integer[] sizes = new Integer[all.size()];
        all.toArray(sizes);
        int chunk = sizes.length / threads;
        if (chunk == 0) {
            chunk = 1;
        }
        List<Thread> ths = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            int from = i * chunk;
            if (from >= sizes.length) {
                break;
            }
            int to = Math.max(from + chunk, sizes.length);
            Task task = new Task(map, sizes, from, to, maxDistance);
            Thread th = new Thread(task);
            th.start();
            ths.add(th);
        }
        for (Thread th : ths) {
            th.join();
        }
        CloneSet cloneSet = new CloneSet();
        Map<Integer, Unit> idMap = createUnitIdMap(units);
        for (Unit unit : units) {
            Map<Integer, Integer> dm = unit.getDistanceMap();
            for (Integer mid : dm.keySet()) {
                int d = dm.get(mid);
                if (d <= maxDistance) {
                    Unit unit2 = idMap.get(mid);
                    List<Clone> cls = toClones(d, unit, unit2);
                    cloneSet.addClones(cls);
                }
                Unit m2 = idMap.get(mid);
                m2.getDistanceMap().put(unit.getId(), Integer.MAX_VALUE);
            }
            unit.clear();
        }
        return cloneSet;
    }

    @Override
    public CloneSet detectClonesType23(String level, int threads, int maxDistance, int minSize, int maxSize) throws Exception {
        switch (level) {
            case "method":
            case "statement":
                CloneSet cs = detectClonesType2(minSize, maxSize);
                CloneSet cs3 = detectClonesType3(threads, maxDistance, minSize, maxSize);
                cs.addClones(cs3.getClones());
                return cs;
            default:
                throw new AssertionError("invalid level: " + level);
        }
    }

    private List<Clone> toClones(int distance, Unit unit, Unit unit2) {
        List<Clone> cls = new ArrayList<>();
        for (Pos p : unit.getPos()) {
            for (Pos p2 : unit2.getPos()) {
                Clone cl = new Clone(distance, new Pos[]{p, p2});
                cls.add(cl);
            }
        }
        return cls;
    }

    private List<Unit> createUnitList(int minSize, int maxSize) {
        List<Unit> units = new ArrayList<>();
        List<TrieNode> nodes = new ArrayList<>();
        nodes.add(root);
        List<Pos[]> positions = new ArrayList<>();
        positions.add(new Pos[0]);
        List<List<Integer>> tokens = new ArrayList<>();
        tokens.add(new ArrayList<>());
        Map<String, Integer> tokenMap = new HashMap<>();
        while (!nodes.isEmpty()) {
            TrieNode node = nodes.remove(0);
            Pos[] pp = positions.remove(0);
            List<Integer> tt = tokens.remove(0);
            if (node.isLeaf()) {
                Pos[] pp2 = filter(pp, minSize, maxSize);
                if (pp2.length == 0) {
                    continue;
                }
                int[] tt2 = toArray(tt);
                Unit u = new Unit(pp2, tt2);
                units.add(u);
                continue;
            }
            for (TrieEdge e : node.getEdges()) {
                nodes.add(e.getDestination());
                positions.add(e.getPositions());
                int tok = toInt(tokenMap, e.getLabel());
                List<Integer> tt2 = new ArrayList<>(tt);
                tt2.add(tok);
                tokens.add(tt2);
            }
        }
        return units;
    }

    private Integer toInt(Map<String, Integer> map, String token) {
        Integer i = map.get(token);
        if (i == null) {
            i = map.size() + 1;
            map.put(token, i);
        }
        return i;
    }

    private Pos[] filter(Pos[] pp, int minSize, int maxSize) {
        int c = 0;
        for (int i = 0; i < pp.length; i++) {
            int lines = pp[i].getLines();
            if (lines >= minSize && lines <= maxSize) {
                c++;
            } else {
                pp[i] = null;
            }
        }
        Pos[] pp2 = new Pos[c];
        int j = 0;
        for (Pos p : pp) {
            if (p != null) {
                pp2[j] = p;
                j++;
            }
        }
        return pp2;
    }

    private int[] toArray(List<Integer> tokens) {
        int[] tt = new int[tokens.size()];
        for (int i = 0; i < tt.length; i++) {
            tt[i] = tokens.get(i);
        }
        return tt;
    }

    private Map<Integer, List<Unit>> createUnitSizeMap(List<Unit> units) {
        Map<Integer, List<Unit>> map = new HashMap<>();
        for (Unit unit : units) {
            int size = unit.getSize();
            List<Unit> uu = map.get(size);
            if (uu == null) {
                uu = new ArrayList<>();
                map.put(size, uu);
            }
            uu.add(unit);
        }
        return map;
    }

    private Map<Integer, Unit> createUnitIdMap(List<Unit> units) {
        Map<Integer, Unit> map = new HashMap<>();
        for (Unit unit : units) {
            map.put(unit.getId(), unit);
        }
        return map;
    }

    @Override
    public Histogram createHistogram() {
        Histogram hist = new Histogram();
        Deque<TrieNode> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            TrieNode p = queue.remove();
            TrieEdge[] edges = p.getEdges();
            for (TrieEdge e : edges) {
                Pos[] pp = e.getPositions();
                hist.storeEdge(pp.length);
            }
            hist.storeNode(edges.length);
        }
        return hist;
    }
}
