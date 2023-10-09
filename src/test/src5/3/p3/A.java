package p3;

public class A {

    public int test(int x, int y) {
        if (x < y) {
            y = x;
        } else {
            x -= y;
        }
        return x + y;
    }

    public long gcd(long p, long q) {
        while (p != q) {
            if (p < q) {
                q -= p;
            } else {
                p -= q;
            }
        }
        return p;
    }
}
