import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class LightWeightProcessB {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private Set<LightWeightProcessB> queue;
    private Set<LightWeightProcessB> response;
    private Boolean sentRequest;
    private int startPort = 3000;
    private ServerSocket server;

    public LightWeightProcessB(int port) {
        try {
            socket = new Socket("localhost", port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            queue = new HashSet<>();
            response = new HashSet<>();
            sentRequest = false;
            startListeningLightWheigt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startListeningLightWheigt(){
        while(true){
            try {
                server = new ServerSocket(startPort);
                break;
            } catch (IOException e) {
                startPort++;
            }
            
        }
    }

    public static void main(String[] args) {
        int port = Integer.valueOf(args[0]);
        LightWeightProcessB processB = new LightWeightProcessB(port);
        
    }
}
