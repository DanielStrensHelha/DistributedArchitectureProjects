import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class LightWeightProcess {
    // For the communication with the other light weights 
    private static final int BROTHER_CONNECTION_STARTING_PORT = 4000;
    private int port;
    private ServerSocket server;
    private ArrayList<Socket> brothers;
    IClockAlgorithm comAlgorithm;

    // For the communication with the heavy process : 
    private Socket socket;
    private DataOutputStream heavyOut;
    private DataInputStream heavyIn;
    private Character algorithmUsed;
    private Thread criticalThread;
    
    // For the communication with the displaying process
    private static final int DISPLAY_PORT = 6969;
    private Socket displaySocket;
    private DataOutputStream displayOut;

    public LightWeightProcess(int port, Character algorithmUsed) {
        this.port = BROTHER_CONNECTION_STARTING_PORT;
        brothers = new ArrayList<Socket>();
        this.algorithmUsed = algorithmUsed;

        // Connect to the heavy process
        try {
            socket = new Socket("localhost", port);
            heavyOut = new DataOutputStream(socket.getOutputStream());
            heavyIn = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
            log("Couldn't find the heavy process on port " + port);
        }

        // Create the ServerSocket
        try {
            server = new ServerSocket(0);
        } catch (IOException e) {e.printStackTrace();}
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
                connectToBrothers(algorithmUsed);
                break;

            case 'W':   // You can write to the console
                log("Telling the algorithm to write to the resource");
                if (this.criticalThread != null && !this.criticalThread.isAlive()) {
                    log("Can't tell them to start cause they didn't stop the last one");
                    break;
                }

                this.criticalThread = new Thread(() -> {
                    try {
                        this.comAlgorithm.writeToResource();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                this.criticalThread.start();
                
                break;

            case 'D':   // Tell me when you're done writing
                if (this.criticalThread != null && this.criticalThread.isAlive())
                    heavyOut.writeChar('N');
                
                else
                    heavyOut.writeChar('Y');
                
                break;

            case '/': // Die        
                log("Dying !");
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
    private void connectToBrothers(Character algorithm) throws IOException {
        List<Integer> ports = new ArrayList<>();
        while (true) {
            int port = heavyIn.readInt();

            if (port == 0)
                break;

            ports.add(port);
        }
        Collections.sort(ports);
        log("Received those ports : " + ports.toString());
        
        Iterator<Integer> it = ports.iterator();        
        // Connect to all precedent ports
        while (it.hasNext()) {
            int port = it.next();
            if (port < this.port) {
                try {brothers.add(new Socket("localhost", port));}
                catch (Exception e) {log(e.getMessage());}
            }
            
            if (port > this.port) {
                server.setSoTimeout(3000);
                brothers.add(server.accept());
                log("Accepted connection from " + port);
            }
        }

        startListeningToLight(algorithm, ports);
    }

    /**
     * Creates a thread for each brother, 
     * which will listen to said brother and take necessary action
     */
    private void startListeningToLight(Character algorithm, List<Integer> ports) {
        if (algorithm.equals('A'))
            comAlgorithm = new Lamport(brothers, this.port, ports, displayOut);
        else if (algorithm.equals('B'))
            comAlgorithm = new Agrawala(brothers, this.port, ports, displayOut);
        else log("Don't know the algorithm :(");
    }

    /**
     * Write to the console, adds the server port to recognize which sub process is writing.
     * @param s
     */
    private void log(String s) {
        System.out.println("[port "+ port + "] " + s);
    }

    private void closeAll() {
        comAlgorithm.closeAll();
    }

    public static void main(String[] args) throws InterruptedException{
        if (args.length < 2) {
            System.out.println("NOT ENOUGH ARGS");
            return;
        }
        int port = Integer.valueOf(args[0]);
        Character algorithm = (args[1].charAt(0));
        
        System.out.println("port : " + port + " alg : " + algorithm);
        LightWeightProcess process = new LightWeightProcess(port, algorithm);

        try {
			while(!process.listenHeavy());
		} catch (Exception e) {e.printStackTrace();} 
        
        finally {
            process.log("Closing every thread");
            process.closeAll();
        }
    }
}
