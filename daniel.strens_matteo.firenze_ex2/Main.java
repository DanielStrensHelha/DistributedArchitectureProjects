import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Main {
    private ServerSocket server = null;
    private Socket socket = null;
    
    public Main(int port) {
        try {
            server = new ServerSocket(port);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Starts listening to client connections, and handles each one of them in a separate thread
     */
    private void startListening() {
        try {
            while (true) {
                socket = server.accept();                
                HandledClient hc = new HandledClient(socket);
                Thread th = new Thread(hc);
                th.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Inner Class, one per handled client
     */
    private class HandledClient implements Runnable {
        private Socket socket = null;
        private BufferedReader in =  null;

        public HandledClient(Socket socket) throws IOException {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String s = in.readLine();
                    System.out.println(s);
                }
            } catch (SocketException e) {
                //Do nothing, client disconnected
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {socket.close();}
                catch (Exception e) {}
            }
        }
    }
    public static void main(String[] args) {
        System.out.println("I am the main screen !\n\n");
        Main server = new Main(5000);
        server.startListening();
    }
}