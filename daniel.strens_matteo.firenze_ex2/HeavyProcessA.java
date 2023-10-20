import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class HeavyProcessA {
    private static Socket socket;
    private static ServerSocket server;
    private static DataOutputStream out;
    private static DataInputStream in;
    private static int token;
    private static Socket[] lightWeights;

    private static int answersfromLightweigth;
    
    private final static String SUB_PROCESS = ProcessHandle.current().info().command()
        .orElse(null);
    private final static int LIGHT_WEIGHT_PORT = 5001;
    private final static int NUM_LIGHTWEIGHTS = 3;

    /**
     * Connects to ProcessB and creates sub processes
     */
    public HeavyProcessA() {
        token = 1;
        answersfromLightweigth = 0;
        lightWeights = new Socket[NUM_LIGHTWEIGHTS];
        
        // Connect to the HeavyWeightProcess and create a server socket for the light weight ones
        try {
            socket = new Socket("localhost", 5000);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            server = new ServerSocket(LIGHT_WEIGHT_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
            try {
                // Create the process
                ProcessBuilder pBuilder = new ProcessBuilder(SUB_PROCESS, Integer.toString(LIGHT_WEIGHT_PORT));
                @SuppressWarnings("unused")
                Process process = pBuilder.start();
                
                // Wait for it to connect and store it into the lightweights array
                lightWeights[i] = server.accept();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    
    // TODO implement this
    /**
     * Asks light weight if they wanna use the resource
     */
    private static void sendActionToLightweight(int lightweight) {
        
    }
    
    // TODO implement this
    /**
     * Wait for all lightweight process to respond to sendActionToLightWeight()
     */
    private static void listenLightweight() {
    }
    
    public static void main(String[] args) {
        System.out.println("Ik ben process A :)");
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        new HeavyProcessA();

        //Enter the main loop
        while(true){
            while(token == 0)
                listenHeavyweight();

            for (int i=0; i < NUM_LIGHTWEIGHTS; i++)
                sendActionToLightweight(i);

            while(answersfromLightweigth < NUM_LIGHTWEIGHTS)
                listenLightweight();

            token = 0;
            sendTokenToHeavyweight();
        }
    }
}
