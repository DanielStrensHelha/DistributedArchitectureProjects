package Ex4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        final int SIZE = 100_000;
        final int TO_SEARCH = 1234;
        final int THREAD_NUMBER = 20;
        //Creating and shuffling an array
        List<Integer> list = new ArrayList<Integer>();
        for(int i = 0; i < SIZE; i++)
            list.add(i);
        
        //Collections.shuffle(list);
        int[] array = list.stream().mapToInt(Integer::intValue).toArray();

         //Setting up a timer
        long startTime = System.nanoTime();

        Searcher.ParallelSearch(TO_SEARCH, array, THREAD_NUMBER);

        //Timer end time
        long endTime = System.nanoTime();

        //Display the duration
        double duration = (double)(endTime - startTime) / 1_000_000;
        System.out.println("Execution time with different threads in Miliseconds: " + duration);
    }
}
