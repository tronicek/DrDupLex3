package p4;

public class A {

    public void m(int x, int y) {
        while (x < y) {
            x *= 2;
        }
        System.out.println(x);
        while (y > 0) {
            y /= 2;
        }
    }

    public void m2(int x, int y) {
        if (x < y) {
            x *= 2;
        }
        System.out.println("x" + x);
    }
}
