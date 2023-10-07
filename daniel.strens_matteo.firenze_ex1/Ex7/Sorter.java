package Ex7;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.stream.IntStream;

public class Sorter {
    /**
     * Sorts the array using mergesort
     * @param array the array to sort
     * @return
     */
    public static int[] sort(int[] array) throws InterruptedException, ExecutionException {
        return sort(array, 2);
    }

    /**
     * Sorts the array using mergesort
     * @param array the array to sort
     * @param divisionLevel the number of time the thread divides itself into other threads
     * @return
     */
    public static int[] sort(int[] array, int divisionLevel) throws InterruptedException, ExecutionException {
        if (divisionLevel > 0){
            //Create two Callable to start at the same time
            RunnableFuture<int[]>[] futures = new RunnableFuture[2];

            int[][] subArrays = new int[2][];
            subArrays[0] = Arrays.copyOfRange(array, 0, array.length/2);
            subArrays[1] = Arrays.copyOfRange(array, array.length/2, array.length);
            
            for (int i = 0; i < 2; i++) {
                final int index=i;

                RunnableFuture<int[]> runnable = new FutureTask<>(new Callable<int[]>() {
                    @Override
                    public int[] call() throws Exception {
                        return sort(subArrays[index], divisionLevel-1);
                    }
                });
                futures[i] = runnable;
            }

            Thread th = new Thread(futures[0]);
            th.start();
            Thread th2 = new Thread(futures[1]);
            th2.start();
            
            //Reassemble the array
            int[] part1 = (int[]) futures[0].get();
            int[] part2 = (int[]) futures[1].get();
            return mergeArrays(part1, part2); 
        }
        else
            return bottomUpMergeSort(array);
    }

    /**
     * Merges arrays with the merge method
     * @param array1
     * @param array2
     * @return An array containing the two inputs merged
     */
    private static int[] mergeArrays(int[] array1, int[] array2) {
        int[] mergedArray = null;
        int middle = 0;

        if (array1.length >= array2.length) {
            mergedArray = IntStream.concat(Arrays.stream(array1), Arrays.stream(array2)).toArray();
            middle = array1.length-1;
        }
        else {
            mergedArray = IntStream.concat(Arrays.stream(array2), Arrays.stream(array1)).toArray();
            middle = array2.length-1;
        }

        // Create working array
        int[] aux = new int[mergedArray.length];

        // Merge the two arrays
        merge(  mergedArray, aux, 0, 
                middle, 
                mergedArray.length-1);
        
        return mergedArray;
    }

    /**
     * Sorts an array of integers using Bottom-Up Merge Sort.
     *
     * @param arr The array of integers to be sorted.
     * @return The sorted array.
     */
    private static int[] bottomUpMergeSort(int[] arr) {
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
