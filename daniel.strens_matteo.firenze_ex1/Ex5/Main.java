package Ex5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        final int SIZE = 125_000_000;
        final int TO_SEARCH = 100_000_000;
        final int THREAD_NUMBER = 500;
        //Creating and shuffling an array
        List<Integer> list = new ArrayList<Integer>();
        for(int i = 0; i < SIZE; i++)
            list.add(i);

        Collections.shuffle(list);
        int[] array = list.stream().mapToInt(Integer::intValue).toArray();

        Searcher.ParallelSearch(TO_SEARCH, array, THREAD_NUMBER);
    }
}
