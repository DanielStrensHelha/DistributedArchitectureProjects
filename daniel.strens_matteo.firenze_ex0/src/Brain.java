import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Brain {
    private ServerSocket server = null;
    private Socket socket = null;
    
    public Brain(int port) {
        try {
            server = new ServerSocket(port);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Starts accepting connections to the ServerSocket.
     * Puts each one of them in a different thread.
     */
    private void startListening() {
        try {
            while (true) {
                socket = server.accept();
                System.out.println("Cell connected :D");
                
                HandledCell hc = new HandledCell(socket);
                Thread th = new Thread(hc);
                th.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Private inner class for which each instance handles a single Cell
     */
    private class HandledCell implements Runnable {
        private Socket socket = null;
        private DataInputStream in =  null;
        private DataOutputStream out = null;
        private String socketName = null;
        private static final DataHandler dh = new DataHandler();

        public HandledCell(Socket socket) throws IOException {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            socketName = socket.toString();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Character instruction = in.readChar();
                    System.out.println("Received : " + instruction);
                    
                    if (instruction.equals('R')) {
                        //If the cell wants to read the value
                        synchronized(dh) {
                            int value = dh.read();
                            System.out.println("Sending back : " + value);
                            out.writeInt(value);
                        }
                    } 
                    else if (instruction.equals('W')) {
                        //If the cell wants to update the value
                        synchronized(dh) {
                            int value = dh.read();
                            System.out.println("Current value : " + value);
                            //first we send back the current value
                            out.writeInt(dh.read());

                            //Then we expect the new value, and update it.
                            int newValue = in.readInt();
                            dh.update(newValue);
                            System.out.println("New value is : " + newValue);
                        }
                    } 
                    else {
                        System.out.println("Unexpectedly received : " + instruction);
                    }
                }
            } catch (SocketException e) {
                System.out.println("Cell Disconnected : "+ socketName);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {socket.close();}
                catch (Exception e) {}
            }
        }

    }
    public static void main(String[] args) {
        Brain brain = new Brain(5000);
        brain.startListening();
    }
}
