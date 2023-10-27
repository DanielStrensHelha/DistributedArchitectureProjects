import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Lamport implements IClockAlgorithm{
    private List<Thread> toClose;
    private HashMap<Socket, Integer> brothers;
    private Boolean shouldClose;
    private int portNumber;
    
    Lamport(HashSet<Socket> brothers, int port) {
        portNumber = port;
        shouldClose = false;
        toClose = new ArrayList<Thread>();
        this.brothers = new HashMap<Socket, Integer>();

        for (Socket socket : brothers) {
            this.brothers.put(socket, Integer.MAX_VALUE);
        }

        startListening();
    }

    @Override
    public void startListening() {
        brothers.forEach((socket, i) -> {
            Thread thLight  = new Thread(() -> {
                while (true) {
                    try {
                        socket.setSoTimeout(3000);
                        DataInputStream sIn = new DataInputStream(socket.getInputStream());

                        synchronized(shouldClose) {
                            if (shouldClose) {
                                return;
                            }
                            listenLight(sIn);
                        }

                    } catch (Exception e) {log("Exception : " + e.getMessage());}
                }
            });

            thLight.start();
            toClose.add(thLight);
        });
    }

    @Override
    public void closeAll() {
        shouldClose = true;
        toClose.forEach(th -> {
            try {
				th.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            System.out.println("Closed thread");
        });
    }

    /**
     * TODO implement this
     * @param sIn
     * @throws IOException
     */
    private void listenLight(DataInputStream sIn) throws IOException {
        Character c = sIn.readChar();
        switch (c) {
            case 'Q': //TODO Question : Can I use the CS ?
                
                break;

            case 'Y': //TODO Yes, you can use the CS !
            
                break;
        
            case 'D': //TODO Done using the CS.

                break;

            default:
                break;
        }
    }

    /**
     * Write to the console, adds the server port to recognize which sub process is writing.
     * @param s
     */
    private void log(String s) {
        System.out.println("[Port:" + this.portNumber + "] > " + s);
    }

}