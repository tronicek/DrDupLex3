package p4;

public class A {

    public int gcd(int x, int y) {
        while (x != y) {
            if (x > y) {
                x -= y;
            } else {
                y -= x;
            }
        }
        return x;
    }

    public int gcd2(int x, int y) {
        while (x != y) {
            if (x < y) {
                y -= x;
            } else {
                x -= y;
            }
        }
        return x;
    }
}
