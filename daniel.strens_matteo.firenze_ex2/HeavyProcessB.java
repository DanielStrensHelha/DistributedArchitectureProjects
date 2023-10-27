import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class HeavyProcessB {
    private static ServerSocket serverSocket;
    private static Socket socket;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static int token = 0;

    public HeavyProcessB() {
        try {
            //Waiting for HeavyProcess A to connect
            serverSocket = new ServerSocket(5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startListening() {
        try {
            System.out.println("Waiting for other process to connect...");
            socket = serverSocket.accept();
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
            TimeUnit.SECONDS.sleep(2);
            sendTokenToHeavyweight();
        }
    }
}
