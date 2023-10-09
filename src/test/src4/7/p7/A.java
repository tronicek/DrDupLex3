package p7;

public class A {

    public void swap(double data[], int i, int j) {
        double t = data[i];
        data[i] = data[j];
        data[j] = t;
    }

    void swap(int a[], int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}
