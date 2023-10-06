import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Cell {    
    private Socket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    /**
     * Constructor
     * @param address example : "localhost" or "127.0.0.1"
     * @param port example : 5000
     */
    public Cell (String address, int port) {
        try {
            //Connecting the socket
            socket = new Socket(address, port);
            System.out.println("Waiting for the start...");
 
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        }
        catch (UnknownHostException u) {
            System.out.println(u);
            return;
        }
        catch (IOException i) {
            System.out.println(i);
            return;
        }
    }

    /**
     * Asks for the current value of the variable, and prints it
     */
    private void readValue() {
        try {
            out.writeChar('R');
            int value = in.readInt();
            System.out.println("The value is currently : " + value);
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the current value of the variable by incrementing it once.
     */
    private void updateValue() {
        try {
            out.writeChar('W');
            int value = in.readInt();
            out.writeInt(value+1);
            System.out.println("Changing value from " + value + " to " + (value+1));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Cell cell = new Cell("localhost", 5000);
        int reps = 10;

        //###############
        //Will start at the first second of the next minute
        final LocalTime startTime = LocalTime.now().plus(1, ChronoUnit.MINUTES);
        while (true) {
            if (LocalTime.now().getMinute() >= startTime.getMinute() && LocalTime.now().getHour() >= startTime.getHour())
                break;
        }
        //###############


        if (args.length >= 2) {
            reps = Integer.parseInt(args[1]);
            for (int i = 0; i<reps; i++) {
                if (args[0].equals("updater"))
                    cell.updateValue();
                else
                    cell.readValue();
                // TimeUnit.MILLISECONDS.sleep(10);
            }
        }
        else {
            for (int i = 0; i < reps; i++) {
                if (args.length == 1 && args[0] == "reader")
                    cell.readValue();
                else cell.updateValue();
            }
        }
    }
}
