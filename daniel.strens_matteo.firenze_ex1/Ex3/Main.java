package Ex3;

import java.util.Collections;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args){
        /*In this part of the code we simply create a list with number and shuffle it.
         We then create an occurrence of the ThreadHandler that take the list and
        * the number we want to find in the shuffled list. And start the method searchNumber
        * We then start*/

        LinkedList<Integer> list = new LinkedList<>();
        for(int i = 1 ; i <= 100000 ; i++)
            list.add(i);
        
        Collections.shuffle(list);

        ThreadHandler th = new ThreadHandler(list,1);
        th.searchNumber();


    }

    /**
     * This class take the list of number that are shuffled and the number we want to find in the list.
     * With his searchNumber function it will find the position of the number in the list.
      */
    static class ThreadHandler{
        private Integer nbr;
        private LinkedList<Integer> list;
        private Integer check = 2;
      public ThreadHandler(LinkedList<Integer> list,Integer number){
          this.list = list;
          this.nbr = number;
      }

        /**
         * @display the two threads (the first to finish is the first printed) as well as the position in which was the number to find
         * @description Basically we create two threads in which we iterate with a for loop and when the number is found we print the result.
         * The first thread iterate the list starting at the beginning and the second thread starting at the end of the list.
         */
        public void searchNumber(){
          try {
              System.out.println("First position :");
              Thread t1 = new Thread(() -> {
                  for (int i = 0; i < list.size(); i++) {
                      if (list.get(i).equals(nbr)) {
                          System.out.println("Thread 1 has found the number! It was at index : "+i+"\n\n");
                          check--;
                          return;
                      }
                  }
              });
              Thread t2 = new Thread(() -> {
                  for (int i = list.size() - 1; i >= 0; i--) {
                      if (list.get(i).equals(nbr)) {
                          System.out.println("Thread 2 has found the number ! It was at index : "+i+"\n\n");
                          check--;
                          return;
                      }
                  }
              });
              t1.start();
              t2.start();

              t1.join();
              t2.join();
              if(check==2){
                  System.out.println("The number was not in the array");
              }
          } catch(Exception e){
              e.getMessage();
          }
      }
    }
}