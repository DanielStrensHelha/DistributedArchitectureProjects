import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class HeavyProcessB {
    private static Socket socket;
    private static ServerSocket server;
    private static DataOutputStream out;
    private static DataInputStream in;
    private static int token;
    private static Socket[] lightWeights;

    private static int answersfromLightweigth;
    
    private final static String SUB_PROCESS = "LightWeightProcessB";
    private final static int LIGHT_WEIGHT_PORT = 5002;
    private final static int NUM_LIGHTWEIGHTS = 3;

     /**
     * Connects to ProcessA and creates sub processes
     */
    public HeavyProcessB() {
        token = 1;
        answersfromLightweigth = 0;
        lightWeights = new Socket[NUM_LIGHTWEIGHTS];
        
        // Connect to the HeavyWeightProcess and create a server socket for the light weight ones
        try {
            socket = new Socket("localhost", 5002);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            server = new ServerSocket(LIGHT_WEIGHT_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            createProcesses();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the subprocesses LightWeightProcessA
     * @throws URISyntaxException
     */
    private void createProcesses() throws URISyntaxException {
        for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
            try {
                // Create the process
                ProcessBuilder pBuilder = new ProcessBuilder("java", SUB_PROCESS, Integer.toString(LIGHT_WEIGHT_PORT));
                @SuppressWarnings("unused")
                Process process = pBuilder.start();

                // Wait for it to connect and store it into the lightweights array
                lightWeights[i] = server.accept();
                System.out.println("Accepted a process :D");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void startListening() {
        try {
            System.out.println("Waiting for other process to connect...");
            socket = server.accept();
            System.out.println("Connected");
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void receiveTokenFromHeavyweight() {
        try {
            token = in.readInt();
            System.out.println("Received: " + token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendHeavyweight() {
        try {
            out.writeInt(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void listenHeavyweight() throws IOException {
        token = in.readInt();
        System.out.println("Received: " + token);
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
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("----- HeavyProcess B -----");
        new HeavyProcessB(); //To initialize static values
        HeavyProcessB.startListening();

        //Enter the main loop
        while(true){
            while(token == 0)
                try {
                    listenHeavyweight();
                } catch (IOException e) {
                    return;
                }

            // for (int i=0; i < NUM_LIGHTWEIGHTS; i++)
            //     sendActionToLightweight(i);

            // while(answersfromLightweigth < NUM_LIGHTWEIGHTS)
            //     listenLightweight();

            token = 0;
            TimeUnit.SECONDS.sleep(20);
            sendTokenToHeavyweight();
        }
    }
}
