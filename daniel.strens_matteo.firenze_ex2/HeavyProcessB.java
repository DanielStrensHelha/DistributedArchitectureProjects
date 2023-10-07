import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HeavyProcessB {
    private static ServerSocket serverSocket;
    private static Socket socket;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static int token = 1;

    public HeavyProcessB() {
        try {
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

    public static void main(String[] args) {
        new HeavyProcessB(); //To initialize static values
        HeavyProcessB.startListening();
        //Add while(true) here
    }
}
