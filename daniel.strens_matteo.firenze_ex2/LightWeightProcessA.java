import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class LightWeightProcessA {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public LightWeightProcessA(int port) {
        try {
            socket = new Socket("localhost", port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = Integer.valueOf(args[0]);
        LightWeightProcessA process = new LightWeightProcessA(port);   
    }
}
