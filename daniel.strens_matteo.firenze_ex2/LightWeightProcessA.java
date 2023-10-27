import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class LightWeightProcessA {
    // For the communication with the other light weights 
    private static final int BROTHER_CONNECTION_STARTING_PORT = 4000;
    private int port;
    private ServerSocket server;
    private HashMap<Socket, Integer> brothers;
    private boolean intoCritical;
    private List<Thread> toClose;
    private Boolean shouldClose;

    // For the communication with the heavy process : 
    private Socket socket;
    private DataOutputStream heavyOut;
    private DataInputStream heavyIn;
    
    // For the communication with the displaying process
    private static final int DISPLAY_PORT = 6969;
    private Socket displaySocket;
    private DataOutputStream displayOut;

    public LightWeightProcessA(int port) {
        this.port = BROTHER_CONNECTION_STARTING_PORT;
        brothers = new HashMap<Socket, Integer>();
        intoCritical = false;
        toClose = new ArrayList<Thread>();
        shouldClose = false;

        // Connect to the heavy process
        try {
            socket = new Socket("localhost", port);
            heavyOut = new DataOutputStream(socket.getOutputStream());
            heavyIn = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
            log("Couldn't find the heavy process on port " + port);
        }

        // Create the ServerSocket
        while (true) {
            try {
                server = new ServerSocket(0);
                break;
            } catch (IOException e) {}
        }
        this.port = server.getLocalPort();

        // Connect to the display process
        try {
            displaySocket = new Socket("localhost", DISPLAY_PORT);
            displayOut = new DataOutputStream(displaySocket.getOutputStream());
        } catch (Exception e) {
            log("Couldn't connect to display port : ");
            e.printStackTrace();
        }
    }

    /**
     * Listen to heavy weight's instructions
     **/ 
    private boolean listenHeavy() throws Exception {
        // Wait for instruction
        Character c = heavyIn.readChar(); 
        switch (c) {
            case 'P':   // Tell your port
                heavyOut.writeInt(port);
                break;
            
            case 'C':   // Connect to brothers and start listening to them
                connectToBrothers();
                break;

            case 'W':   //TODO You can write to the console
                writeToDisplay("(LightWeightProcessA" + this.port + ") Unimplemented at the moment");
                break;

            case 'D':   // Tell me when you're done writing
                heavyOut.writeChar('Y'); //TODO For now, say yes
                break;

            case '/': // Die
                return true;
            
            default:
                break;
        }
        return false;
    }

    /**
     * Get the ports to connect to from heavy process,
     * Connect to them.
     * @throws IOException
     */
    private void connectToBrothers() throws IOException {
        List<Integer> ports = new ArrayList<>();
        while (true) {
            int port = heavyIn.readInt();

            if (port == 0)
                break;

            ports.add(port);
        }
        Collections.sort(ports);
        Iterator<Integer> it = ports.iterator();

        // Connect to all precedent ports
        while (it.hasNext()) {
            int port = it.next();
            if (port >= this.port)
                break;
            
            try {brothers.put(new Socket("localhost", port), Integer.MAX_VALUE);}
            catch (Exception e) {log(e.getMessage());}
        }

        // Wait for the next ports to connect
        while (it.hasNext()) {
            int port = it.next();
            server.setSoTimeout(3000);
            brothers.put(server.accept(), Integer.MAX_VALUE);
            log("Accepted connection from " + port);
        }

        startListeningToLight();
    }

    /**
     * Creates a thread for each brother, 
     * which will listen to said brother and take necessary action
     */
    private void startListeningToLight() {
        brothers.forEach((socket, i) -> {
            Thread thLight  = new Thread(() -> {
                try {
                    socket.setSoTimeout(3);

					DataInputStream sIn = new DataInputStream(socket.getInputStream());
                    while(true) {
                        synchronized(shouldClose) {
                            if (shouldClose)
                                return;
                        }

                        listenLight(sIn);
                    }
				} catch (Exception e) {log("Exception : " + e.getCause());}
            });

            thLight.start();
            toClose.add(thLight);
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
        System.out.println("[Port:" + this.port + "] > " + s);
    }

    /**
     * Tell the bros we're going to write on the console
     */
    private void doAction() {
        
    }

    /**
     * Write to the display entity
     * @param s
     */
    private void writeToDisplay(String s) {
        try {
            displayOut.writeChars(s + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeAll() {
        shouldClose = true;
        toClose.forEach(th -> {
            try {
				th.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        });
    }

    public static void main(String[] args){
        int port = Integer.valueOf(args[0]);
        LightWeightProcessA process = new LightWeightProcessA(port);

        try {
			while(!process.listenHeavy());
		} catch (Exception e) {} 
        
        finally {
            process.closeAll();
        }

    }
}
