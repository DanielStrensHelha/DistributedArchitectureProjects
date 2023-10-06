import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class Client {
    private Socket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    public Client (String address, int port) {
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");
 
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

    private int sendMsg(int i) {
        try {
            // Send an integer to the server
            out.writeInt(i);
            System.out.println("Sent Integer: " + i);
            
            // Receive a response from the server
            int receivedResponse = in.readInt();
            System.out.println("Received Response: " + receivedResponse);
            return receivedResponse;
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    public static void main(String[] args) {
        Client c = new Client("localhost", 5000);
        for (int i = 2; i<120; i++) {
            c.sendMsg(i);
            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
                //TODO exit the program or smth
            }
        }
    }
}
