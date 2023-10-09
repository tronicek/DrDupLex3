package p2;

public class A {

    int sum(int[][] m) {
        int s = 0;
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                s += m[i][j];
            }
        }
        return s;
    }

    int product(int[][] m) {
        int p = 0;
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                p *= m[i][j];
            }
        }
        return p;
    }
}
