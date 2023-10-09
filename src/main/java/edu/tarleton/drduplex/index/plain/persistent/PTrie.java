package edu.tarleton.drduplex.index.plain.persistent;

import edu.tarleton.drduplex.Histogram;
import edu.tarleton.drduplex.clones.Clone;
import edu.tarleton.drduplex.clones.CloneSet;
import edu.tarleton.drduplex.clones.Pos;
import edu.tarleton.drduplex.index.Unit;
import edu.tarleton.drduplex.index.PIndex;
import edu.tarleton.drduplex.index.Task;
import edu.tarleton.drduplex.index.plain.Trie;
import edu.tarleton.drduplex.index.plain.TrieEdge;
import edu.tarleton.drduplex.index.plain.TrieNode;
import java.io.IOException;
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
 * The class that represents the plain (not compressed) persistent TRIE.
 *
 * @author Zdenek Tronicek
 */
public class PTrie implements AutoCloseable, PIndex {

    private final Storage storage;
    private final PFilePaths filePaths;
    private final PLabels labels;
    private final PNextStmtMap nextStmtMap;

    private PTrie(Storage storage) throws IOException {
        this.storage = storage;
        filePaths = PFilePaths.load(storage);
        labels = PLabels.load(storage);
        nextStmtMap = PNextStmtMap.load(storage);
    }

    public static PTrie initialize(String nodeFileName, int nodeFilePageSize,
            String edgeFileName, int edgeFilePageSize,
            String posFileName, int posFilePageSize,
            String pathFileName, String labelFileName,
            String nextStmtMapFileName) throws IOException {
        Storage st = Storage.initialize(nodeFileName, nodeFilePageSize,
                edgeFileName, edgeFilePageSize,
                posFileName, posFilePageSize,
                pathFileName, labelFileName,
                nextStmtMapFileName);
        PNode.reset();
        PEdgeBlock.reset();
        PPosBlock.reset();
        PNode root = new PNode();
        root.writeTo(st);
        PFilePaths.initialize(st);
        PLabels.initialize(st);
        PNextStmtMap.initialize(st);
        return new PTrie(st);
    }

    public static PTrie fromFiles(String nodeFileName, int nodeFilePageSize,
            String edgeFileName, int edgeFilePageSize,
            String posFileName, int posFilePageSize,
            String pathFileName, String labelFileName,
            String nextStmtMapFileName) throws IOException {
        Storage st = Storage.open(nodeFileName, nodeFilePageSize,
                edgeFileName, edgeFilePageSize,
                posFileName, posFilePageSize,
                pathFileName, labelFileName,
                nextStmtMapFileName);
        return new PTrie(st);
    }

    @Override
    public void close() throws Exception {
        labels.store(storage);
        if (nextStmtMap != null) {
            nextStmtMap.store(storage);
        }
        storage.close();
    }

    public void addTrie(Trie trie) throws IOException {
        TrieNode root = trie.getRoot();
        root.setPersistentId(0L);
        Deque<TrieNode> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            TrieNode n = queue.remove();
            PNode p = new PNode(n.getPersistentId());
            p.readFrom(storage);
            PEdgeBlock eb = p.getEdgeBlock();
            for (TrieEdge e : n.getEdges()) {
                short labelId = (short) labels.toLabelId(e.getLabel());
                PEdge pe = eb.findEdge(labelId);
                if (pe == null) {
                    PNode dest = new PNode();
                    dest.writeTo(storage);
                    pe = new PEdge(labelId, dest.getId());
                    eb.addEdge(pe);
                }
                for (Pos pos : e.getPositions()) {
                    long fileId = filePaths.toFileId(storage, pos.getFile());
                    PPos pp = new PPos(fileId, pos.getStart(), pos.getEnd());
                    pe.addPos(pp);
                }
                TrieNode nn = e.getDestination();
                nn.setPersistentId(pe.getDestId());
                queue.add(nn);
            }
            p.writeTo(storage);
        }
        if (nextStmtMap != null) {
            nextStmtMap.addNextStmtMap(storage, trie.getNextStmtMap());
        }
    }

    @Override
    public void print() throws IOException {
        PNode n = new PNode(0L);
        n.readFrom(storage);
        Deque<PNode> queue = new ArrayDeque<>();
        queue.add(n);
        while (!queue.isEmpty()) {
            PNode p = queue.remove();
            p.print(labels);
            PEdgeBlock eb = p.getEdgeBlock();
            while (eb != null) {
                PEdge[] ee = eb.getEdges();
                for (int i = 0; i < eb.getEdgeCount(); i++) {
                    PEdge e = ee[i];
                    PNode q = new PNode(e.getDestId());
                    q.readFrom(storage);
                    queue.add(q);
                }
                eb = eb.getNext();
            }
        }
        System.out.println("---------------------");
        filePaths.print(storage);
        System.out.println("---------------------");
        if (nextStmtMap != null) {
            nextStmtMap.print(storage);
            System.out.println("---------------------");
        }
    }

    @Override
    public CloneSet detectClonesType2(String level, int minSize, int maxSize) throws IOException {
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

    private CloneSet detectClonesType2(int minSize, int maxSize) throws IOException {
        CloneSet cloneSet = new CloneSet();
        PNode n = new PNode(0L);
        n.readFrom(storage);
        Deque<PNode> queue = new ArrayDeque<>();
        Deque<PEdge> edges = new ArrayDeque<>();
        PEdgeBlock eb = n.getEdgeBlock();
        while (eb != null) {
            PEdge[] ee = eb.getEdges();
            for (int i = 0; i < eb.getEdgeCount(); i++) {
                PEdge e = ee[i];
                PNode dest = new PNode(e.getDestId());
                dest.readFrom(storage);
                queue.add(dest);
                edges.add(e);
            }
            eb = eb.getNext();
        }
        while (!queue.isEmpty()) {
            PNode p = queue.remove();
            PEdge iedge = edges.remove();
            PEdgeBlock block = p.getEdgeBlock();
            while (block != null) {
                PPosBlock posBlock = iedge.getPosBlock();
                if (posBlock != null) {
                    PPos[] pp = posBlock.getPositions(minSize, maxSize);
                    if (pp.length > 1) {
                        Pos[] fpp = toFilePos(pp);
                        Clone cl = new Clone(0, fpp);
                        cloneSet.addClone(cl);
                    }
                }
                PEdge[] ee = block.getEdges();
                for (int i = 0; i < block.getEdgeCount(); i++) {
                    PEdge edge = ee[i];
                    PNode q = new PNode(edge.getDestId());
                    q.readFrom(storage);
                    queue.add(q);
                    edges.add(edge);
                }
                block = block.getNext();
            }
        }
        return cloneSet;
    }

    private Pos[] toFilePos(PPos[] pp) {
        Map<Long, String> map = filePaths.getInverseMap();
        Pos[] fpp = new Pos[pp.length];
        for (int i = 0; i < pp.length; i++) {
            PPos p = pp[i];
            String fn = map.get(p.getFileId());
            fpp[i] = new Pos(fn, p.getBegin(), p.getEnd());
        }
        return fpp;
    }

    private CloneSet detectMergedStmtClonesType2(int minSize, int maxSize) throws IOException {
        List<Clone> cls = new ArrayList<>();
        PNode n = new PNode(0L);
        n.readFrom(storage);
        Deque<PNode> queue = new ArrayDeque<>();
        Deque<PEdge> edges = new ArrayDeque<>();
        PEdgeBlock eb = n.getEdgeBlock();
        while (eb != null) {
            PEdge[] ee = eb.getEdges();
            for (int i = 0; i < eb.getEdgeCount(); i++) {
                PEdge e = ee[i];
                PNode dest = new PNode(e.getDestId());
                dest.readFrom(storage);
                queue.add(dest);
                edges.add(e);
            }
            eb = eb.getNext();
        }
        while (!queue.isEmpty()) {
            PNode p = queue.remove();
            PEdge edge = edges.remove();
            PEdgeBlock block = p.getEdgeBlock();
            while (block != null) {
                PPosBlock posBlock = edge.getPosBlock();
                if (posBlock != null) {
                    PPos[] pp = edge.getPosBlock().getPositions();
                    if (pp.length > 1) {
                        Pos[] fpp = toFilePos(pp);
                        Clone cl = new Clone(0, fpp);
                        cls.add(cl);
                    }
                }
                PEdge[] ee = block.getEdges();
                for (int i = 0; i < block.getEdgeCount(); i++) {
                    PEdge e = ee[i];
                    PNode q = new PNode(e.getDestId());
                    q.readFrom(storage);
                    queue.add(q);
                    edges.add(e);
                }
                block = block.getNext();
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
                Pos next = nextStmtMap.getNext(pp[i]);
                Set<Pos> set = posMap.get(next);
                if (set == null) {
                    continue;
                }
                Set<Integer> inds = new TreeSet<>();
                for (int j = i + 1; j < pp.length; j++) {
                    Pos next2 = nextStmtMap.getNext(pp[j]);
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
                    Pos next2 = nextStmtMap.getNext(pp[j]);
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

    private List<Unit> createUnitList(int minSize, int maxSize) throws IOException {
        PNode root = new PNode(0L);
        root.readFrom(storage);
        List<Unit> mm = new ArrayList<>();
        List<PNode> nodes = new ArrayList<>();
        nodes.add(root);
        List<PPos[]> positions = new ArrayList<>();
        positions.add(new PPos[0]);
        List<List<Integer>> tokens = new ArrayList<>();
        tokens.add(new ArrayList<>());
        Map<Long, String> fileIdMap = filePaths.getInverseMap();
        while (!nodes.isEmpty()) {
            PNode node = nodes.remove(0);
            PPos[] pp = positions.remove(0);
            List<Integer> tt = tokens.remove(0);
            if (node.isLeaf()) {
                PPos[] pp2 = filter(pp, minSize, maxSize);
                if (pp2.length == 0) {
                    continue;
                }
                Pos[] pos = toPos(fileIdMap, pp2);
                int[] tt2 = toArray(tt);
                Unit m = new Unit(pos, tt2);
                mm.add(m);
                continue;
            }
            PEdgeBlock eb = node.getEdgeBlock();
            while (eb != null) {
                PEdge[] ee = eb.getEdges();
                for (int i = 0; i < eb.getEdgeCount(); i++) {
                    PEdge e = ee[i];
                    PNode p = new PNode(e.getDestId());
                    p.readFrom(storage);
                    nodes.add(p);
                    PPosBlock pb = e.getPosBlock();
                    PPos[] pos = (pb == null) ? new PPos[0] : pb.getPositions();
                    positions.add(pos);
                    int tok = e.getLabelId();
                    List<Integer> tt2 = new ArrayList<>(tt);
                    tt2.add(tok);
                    tokens.add(tt2);
                }
                eb = eb.getNext();
            }
        }
        return mm;
    }

    private PPos[] filter(PPos[] pp, int minSize, int maxSize) {
        int c = 0;
        for (int i = 0; i < pp.length; i++) {
            int lines = pp[i].getLines();
            if (lines >= minSize && lines <= maxSize) {
                c++;
            } else {
                pp[i] = null;
            }
        }
        PPos[] pp2 = new PPos[c];
        int j = 0;
        for (PPos p : pp) {
            if (p != null) {
                pp2[j] = p;
                j++;
            }
        }
        return pp2;
    }

    private Pos[] toPos(Map<Long, String> fileIdMap, PPos[] pp) {
        Pos[] pp2 = new Pos[pp.length];
        for (int i = 0; i < pp2.length; i++) {
            PPos p = pp[i];
            String file = fileIdMap.get(p.getFileId());
            pp2[i] = new Pos(file, p.getBegin(), p.getEnd());
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
    public Histogram createHistogram() throws IOException {
        Histogram hist = new Histogram();
        PNode n = new PNode(0L);
        n.readFrom(storage);
        Deque<PNode> queue = new ArrayDeque<>();
        queue.add(n);
        while (!queue.isEmpty()) {
            PNode p = queue.remove();
            int edges = 0;
            PEdgeBlock eb = p.getEdgeBlock();
            while (eb != null) {
                PEdge[] ee = eb.getEdges();
                for (int i = 0; i < eb.getEdgeCount(); i++) {
                    PEdge e = ee[i];
                    PPosBlock pb = e.getPosBlock();
                    if (pb != null) {
                        int c = pb.countPositions();
                        hist.storeEdge(c);
                    }
                    PNode q = new PNode(e.getDestId());
                    q.readFrom(storage);
                    queue.add(q);
                }
                edges += eb.getEdgeCount();
                eb = eb.getNext();
            }
            hist.storeNode(edges);
        }
        return hist;
    }
}
