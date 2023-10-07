import java.io.*;
import java.net.*;

public class HeavyProcessA {
    private static Socket socket;
    private static DataOutputStream out;
    private static DataInputStream in;
    private static int token;

    private static int answersfromLightweigth;
    private final static int NUM_LIGHTWEIGHTS = 3;

    public HeavyProcessA() {
        token = answersfromLightweigth = 0;
        
        try {
            socket = new Socket("localhost", 5000);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Signal to other heavy weight process that we're done using the resource
     */
    public static void sendTokenToHeavyweight() {
        try {
            out.writeInt(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read the token sent by other heavy weight process
     */
    public static void listenHeavyweight() {
        try {
            token = in.readInt();
            System.out.println("Received: " + token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        while(true){
            while(token == 0) {
                listenHeavyweight();
            }
            for (int i=0; i < NUM_LIGHTWEIGHTS; i++) {
                sendActionToLightweight();
            }
            while(answersfromLightweigth < NUM_LIGHTWEIGHTS) {
                listenLightweight();
            }
            token = 0;
            sendTokenToHeavyweight();
        }
        
    }

    // TODO implement this
    /**
     * Asks light weight if they wanna use the resource
     */
    private static void sendActionToLightweight() {
    }

    // TODO implement this
    /**
     * Wait for all lightweight process to respond to sendActionToLightWeight()
     */
    private static void listenLightweight() {
    }
}
