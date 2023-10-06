import java.io.*;
import java.net.*;

public class MainServer {
    private ServerSocket server = null;
    private Socket socket = null;
    
    public MainServer(int port) {
        try {
            server = new ServerSocket(port);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void startListening() {
        try {
            while (true) {
                socket = server.accept();
                System.out.println("Client connected :D");
                
                HandledClient hc = new HandledClient(socket);
                Thread th = new Thread(hc);
                th.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class HandledClient implements Runnable {
        private Socket socket = null;
        private DataInputStream in =  null;
        private DataOutputStream out = null;

        public HandledClient(Socket socket) throws IOException {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int nbr = in.readInt();
                    System.out.println("Received : " + nbr);
                    out.writeInt(nbr+1);
                }
            } catch (SocketException e) {
                System.out.println("Client Disconnected");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {socket.close();}
                catch (Exception e) {}
            }
        }

    }
    public static void main(String[] args) {
        MainServer server = new MainServer(5000);
        server.startListening();
    }
}