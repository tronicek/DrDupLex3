package p6;

public class B {

    public static void sort(long[] array, int[] index, int left, int right) {
        if (left < right) {
            int middle = partition(array, index, left, right);
            sort(array, index, left, middle - 1);
            sort(array, index, middle, right);
        }
    }

    private static int partition(long[] array, int[] index, int left, int right) {
        return 0;
    }
}
