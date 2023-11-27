package Ex7;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class Sorter {
    static ExecutorService executor = Executors.newFixedThreadPool(1000);
    private static final ForkJoinPool pool = new ForkJoinPool();
    private static int[] aux;

    /**
     * Sorts the array using a threaded merge sort 
     * @param array the array to sort
     * @return a sorted copy of the given array
     */
    public static int[] sort(int[] array) {
        int[] newArray = Arrays.copyOf(array, array.length);
        aux = new int[array.length];
        
        sort(newArray, 0, array.length);
        return newArray;
    }

    /**
     * Sorts the specified subregion of the given array using threaded merge sort
     * @param array the array to sort
     * @param offset beginning cell of the subregion
     * @param size size of the subregion
     */
    public static void sort(int[] array, int offset, int size) {
        // Preconditions
        if (array.length <= 1) return;
        if (offset < 0 || size < 1) throw new InvalidParameterException("Offset must be >= 0 and size >= 1");
        if (offset + size > array.length) throw new InvalidParameterException("Got a size + offset bigger than the array size");

        pool.invoke(new SortTask(array, offset, size));
    }

    private static class SortTask extends RecursiveAction {
        private final int[] array;
        private final int offset;
        private final int size;

        SortTask(int[] array, int offset, int size) {
            this.array = array;
            this.offset = offset;
            this.size = size;
        }

        @Override
        protected void compute() {
            if (size > 2) {
                int off1 = offset;
                int off2 = offset + (size / 2);

                int size1 = size / 2;
                int size2 = size - size1;

                invokeAll(new SortTask(array, off1, size1), new SortTask(array, off2, size2));

                mergeArrays(array, off1, size1, off2, size2);
            } else {
                if (array[offset] > array[offset + 1]) {
                    // swap them
                    array[offset] = array[offset] ^ array[offset + 1];
                    array[offset + 1] = array[offset] ^ array[offset + 1];
                    array[offset] = array[offset] ^ array[offset + 1];
                }
            }
        }
    }


    /**
     * Merges arrays with the merge method
     * @param array1
     * @param array2
     * @return An array containing the two inputs merged
     */
    private static void mergeArrays(int[] array, int off1, int size1, int off2, int size2) {
        int low = off1;
        int middle = size1+off1 - 1;
        int high = size2 + off2 - 1;
    
        // Copy the elements to be merged into the auxiliary array
        System.arraycopy(array, low, aux, low, high - low + 1);
    
        // Merge the two arrays
        merge(array, aux, low, middle, high);
    }

    /**
     * Sorts an array of integers using Bottom-Up Merge Sort.
     *
     * @param arr The array of integers to sort.
     * @return The sorted array.
     */
    public static int[] bottomUpMergeSort(int[] arr) {
        int[] aux = new int[arr.length];
        int n = arr.length;

        for (int subarraySize = 1; subarraySize < n; subarraySize *= 2) {
            for (int low = 0; low < n - subarraySize; low += subarraySize * 2) {
                int mid = low + subarraySize - 1;
                int high = Math.min(low + subarraySize * 2 - 1, n - 1);

                merge(arr, aux, low, mid, high);
            }
        }

        return arr;
    }

    /**
     * Merges two subarrays within the array.
     *
     * @param arr  The array containing the subarrays.
     * @param aux  An auxiliary array for merging.
     * @param low  The lower index of the first subarray.
     * @param mid  The upper index of the first subarray.
     * @param high The upper index of the second subarray.
     */
    private static void merge(int[] arr, int[] aux, int low, int mid, int high) {
        // Copy data to auxiliary array
        for (int k = low; k <= high; k++) {
            aux[k] = arr[k];
        }

        int i = low;
        int j = mid + 1;

        for (int k = low; k <= high; k++) {
            if (i > mid) {
                arr[k] = aux[j++];
            } else if (j > high) {
                arr[k] = aux[i++];
            } else if (aux[i] <= aux[j]) {
                arr[k] = aux[i++];
            } else {
                arr[k] = aux[j++];
            }
        }
    }
}
