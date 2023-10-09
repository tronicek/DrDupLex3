package p1;

public class A {

    int x, y;

    void m1() {
        while (x < 0) {
            compute();
        }
        y++;
    }

    void m2() {
        while (x < 0) {
            compute();
            y++;
        }
    }

    void compute() {
    }
}
