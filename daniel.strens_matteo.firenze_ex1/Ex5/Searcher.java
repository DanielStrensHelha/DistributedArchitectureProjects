package Ex5;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class Searcher{
    /**
     * This function will look for an integer inside the provided array.
     * @param toSearch The int to search
     * @param Array The array in which to look for the int
     * @param NumThreads The max number of threads used to solve the problem
     * @return the position of the first occurence of the int, -1 if it wasn't found
     */
    public static int ParallelSearch(int toSearch, int[] Array, int NumThreads) {
        //Figure out how many threads we actually need, and the size of the subarrays
        final int PART_COUNT = (Array.length > NumThreads) ? NumThreads : Array.length;
        final int PART_SIZE = Array.length/PART_COUNT;
        final int REMAINDER = Array.length % PART_COUNT; //To be distributed among the first parts

        //Give part of the array to each thread
        List<RunnableFuture<Integer>> partsArray = new ArrayList<>();
        int arrayIndex = 0;
        for(int i = 0; i < PART_COUNT; i++) { 
            //final attributes (so they can be used in the callable)
            //note : I specified 'final' for clarity, though it is already effectively final
            final int CURRENT_SIZE = PART_SIZE + ((i < REMAINDER) ? 1 : 0);
            final int ARRAY_START_POINT = arrayIndex;
            final int THREAD_NUMBER = i+1;

            RunnableFuture<Integer> runnablePart = new FutureTask<>(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return Searcher.search(Array, toSearch, ARRAY_START_POINT, CURRENT_SIZE, THREAD_NUMBER);
                }
            });
            arrayIndex += CURRENT_SIZE;
            
            //Start this runnable and add it to our array of runnables.
            Thread th = new Thread(runnablePart);
            th.start();
            partsArray.add(runnablePart);
        }

        //Get the results and display if / when we find something
        for (RunnableFuture<Integer> runnableFuture : partsArray) {
            try {
                Integer result = runnableFuture.get();
                if (result >= 0) {
                    System.out.println("(Out of the thread) Found the number at " + result);
                    return result;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Number " + toSearch + " hasn't been found in the array :(");
        return -1;
    }

    /**
     * Used by the runnables to search for the number
     * @param array the array in which to look
     * @param toSearch the number we want to find
     * @param offset the offset of the cell we start at
     * @param threadNumber the number of the thread to display if we find it
     * @return the cell in which we found the number (or -1 if we didn't)
     */
    private static int search(int[] array, int toSearch, int offset, int size, int threadNumber) {
        if (array == null)
            throw new RuntimeException("Given array is null");
        
        for (int i = 0; i < size; i++)
            if (array[i+offset] == toSearch) {
                System.out.println("Found " + toSearch + " in box " + (i+offset) + " by thread number " + threadNumber);
                return i + offset;
            }

        return -1;
    }

}