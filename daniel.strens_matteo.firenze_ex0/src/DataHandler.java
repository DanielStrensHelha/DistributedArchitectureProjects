public class DataHandler {
    private int sharedVariable;

    public DataHandler() {
        this.sharedVariable = 0;
    }

    /**
     * 
     * @return the value of the shared variable
     */
    public int read() {
        return this.sharedVariable;
    }

    /**
     * Update the value of the shared variable
     * @param newValue The new value for the shared variable
     */
    public void update(int newValue) {
        this.sharedVariable = newValue;
    }
}
