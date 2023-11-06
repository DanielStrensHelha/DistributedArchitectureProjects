public interface IClockAlgorithm {
    /**
     * Close all subprocesses in order to quit the program properly
     */
    void closeAll();
    
    /**
     * Start listening to the brother processes
     * Start being able to receive messages
     */
    void startListening();

    /**
     * Ask for permission to write to the console, and do so.
     */
    void writeToResource() throws InterruptedException;

}
