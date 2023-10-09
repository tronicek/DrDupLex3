package p2;

public class A {

    int x, y;

    void compute() {
        System.out.println(x);
        x++;
        y--;
        System.out.println(y);
    }

    void compute2() {
        System.out.println(x);
        x--;
        y++;
        System.out.println(y);
    }
}
