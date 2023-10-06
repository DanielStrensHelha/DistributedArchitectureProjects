package Ex7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {
    private final static int SIZE = 34_000_000;

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
        int[] sortedArray = Sorter.sort(array);

        //Timer end time
        long endTime = System.nanoTime();

        //Display the duration
        double duration = (double)(endTime - startTime) / 1_000_000;
        System.out.println("Execution time with different threads in Miliseconds: " + duration);
        System.out.println("Array sorted !");

        /****
         * SECOND PART
         * sequential use of merge sort
         */
         //Shuffle array
        // array = list.stream().mapToInt(Integer::intValue).toArray();
        
        //Setting up a timer
        startTime = System.nanoTime();

        //Sorting the array
        System.out.println("Array created and shuffled.\nSorting the array...");
        sortedArray = Sorter.sort(array, 0); // <--by setting the divisionLevel to 0, 1 thread will be used

        //Timer end time
        endTime = System.nanoTime();

        //Display the duration
        duration = (double)(endTime - startTime) / 1_000_000;
        System.out.println("Execution time without threads in Miliseconds: " + duration);
        System.out.println("Array sorted !");

    }
}
