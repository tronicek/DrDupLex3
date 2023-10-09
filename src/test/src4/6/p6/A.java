package p6;

public class A {

    public static void sort(int[] array, int[] index, int left, int right) {
        if (left < right) {
            int middle = partition(array, index, left, right);
            sort(array, index, left, middle);
            sort(array, index, middle + 1, right);
        }
    }

    private static int partition(int[] array, int[] index, int left, int right) {
        return 0;
    }
}
