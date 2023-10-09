package edu.tarleton.drduplex.index;

import java.util.List;
import java.util.Map;

/**
 * The task for one thread.
 *
 * @author Zdenek Tronicek
 */
public class Task implements Runnable {

    private final Map<Integer, List<Unit>> map;
    private final Integer[] sizes;
    private final int from;
    private final int to;
    private final int maxDistance;

    public Task(Map<Integer, List<Unit>> map, Integer[] sizes, int from, int to, int maxDistance) {
        this.map = map;
        this.sizes = sizes;
        this.from = from;
        this.to = to;
        this.maxDistance = maxDistance;
    }

    @Override
    public void run() {
        for (int i = from; i < to; i++) {
            Integer d1 = sizes[i];
            List<Unit> mm = map.get(d1);
            for (Integer d2 : sizes) {
                int md = Math.abs(d1 - d2);
                if (md > maxDistance) {
                    continue;
                }
                List<Unit> mm2 = map.get(d2);
                for (Unit m : mm) {
                    for (Unit m2 : mm2) {
                        m.computeDistanceTo(maxDistance, m2);
                    }
                }
            }
            for (Unit m : mm) {
                m.completed();
            }
        }
    }
}
