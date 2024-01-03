package Ex7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {
    private final static int SIZE = 500_000;

    public static void main(String[] args) throws InterruptedException, ExecutionException {        
        //Shuffling the array
        List<Integer> list = new ArrayList<>();
        for (int i=0; i<SIZE; i++)
            list.add(i);
        Collections.shuffle(list);

        /*****
         * FIRST PART
         * using threads
         */
        int[] array = list.stream().mapToInt(Integer::intValue).toArray();
        //Array shuffled

        //Setting up a timer
        long startTime = System.nanoTime();

        //Sorting the array
        System.out.println("Array created and shuffled.\nSorting the array...");
        @SuppressWarnings("unused")
        int[] sortedArray = Sorter.sort(array);
        
        /*
        System.out.print("Sorted array : [");
        for (int i = 0; i < sortedArray.length; i++) {
            System.out.print(" " + sortedArray[i]);
        }
        System.out.println(" ]");
        ////////////////*/

        //End timer and display result
        long endTime = System.nanoTime();
        double duration = (double)(endTime - startTime) / 1_000_000;
        
        System.out.println("Execution time with different threads in Miliseconds: " + duration);
        System.out.println("Array sorted !");

        /****
         * SECOND PART
         * sequential use of merge sort
         */
        //Shuffle array
        array = list.stream().mapToInt(Integer::intValue).toArray();
        
        //Setting up a timer
        startTime = System.nanoTime();

        //Sorting the array
        System.out.println("Array created and shuffled.\nSorting the array...");
        sortedArray = Sorter.bottomUpMergeSort(array);

        //End timer and display results
        endTime = System.nanoTime();
        duration = (double)(endTime - startTime) / 1_000_000;
        
        System.out.println("Execution time without threads in Miliseconds: " + duration);
        System.out.println("Array sorted !");

        /*
        System.out.print("Sorted array : [");
        for (int i = 0; i < sortedArray.length; i++) {
            System.out.print(" " + sortedArray[i]);
        }
        System.out.println(" ]");
        ////////////////*/

    }
}
