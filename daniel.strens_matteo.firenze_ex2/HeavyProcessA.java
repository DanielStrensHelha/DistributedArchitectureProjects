import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class HeavyProcessA {
    private static Socket socket;
    private static ServerSocket server;
    private static DataOutputStream out;
    private static DataInputStream in;
    private static int token;
    private static HashMap<Socket, DataOutputStream> lightWeightsOut;
    private static HashMap<Socket, DataInputStream> lightWeightsIn;

    private static int answersfromLightweigth;
    
    private final static String SUB_PROCESS = "LightWeightProcessA";
    private final static int LIGHT_WEIGHT_PORT = 5001;
    private final static int NUM_LIGHTWEIGHTS = 3;

    /**
     * Connects to ProcessB and creates sub processes
     * @throws IOException
     */
    public HeavyProcessA() throws IOException {
        token = 1;
        answersfromLightweigth = 0;
        lightWeightsOut = new HashMap<Socket, DataOutputStream>();
        lightWeightsIn = new HashMap<Socket, DataInputStream>();
        
        // Connect to the HeavyWeightProcess and create a server socket for the light weight ones
        try {
            socket = new Socket("localhost", 5000);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            server = new ServerSocket(LIGHT_WEIGHT_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        createProcesses();
    }

    /**
     * Create the subprocesses LightWeightProcessA and make them connect to each other
     * @throws IOException
     * @throws URISyntaxException
     */
    private void createProcesses() throws IOException {
        // Create them
        for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
            try {
                // Create the process
                ProcessBuilder pBuilder = new ProcessBuilder("java",  
                    SUB_PROCESS,
                    Integer.toString(LIGHT_WEIGHT_PORT)).inheritIO();
                @SuppressWarnings("unused")
                Process process = pBuilder.start();

                // Wait for it to connect and store it into the lightweights array
                Socket s = server.accept();
                lightWeightsOut.put(s, new DataOutputStream(s.getOutputStream()));
                lightWeightsIn.put(s, new DataInputStream(s.getInputStream()));

                System.out.println("Accepted a process :D");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Connect processes
        final Set<Integer> ports = getPorts();
        System.out.println(ports.toString());

        // Tell them who to connect to
        lightWeightsOut.forEach((socket, sOut) -> {
            try {
                sOut.writeChar('C'); // Tell the process it has to connect to those ports
                ports.forEach( p -> {
                    try {sOut.writeInt(p);} 
                    catch (IOException e) {e.printStackTrace();}
                });
                sOut.writeInt(0); // Stop receiving ports
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Ask each light weight their port number
     * @throws IOException
     */
    private Set<Integer> getPorts() throws IOException {
        Set<Integer> ports = new HashSet<Integer>();
        Iterator<Socket> it = lightWeightsOut.keySet().iterator();

        while(it.hasNext()) {
            Socket s = it.next();
            DataInputStream sIn = new DataInputStream(s.getInputStream());
            DataOutputStream sOut = new DataOutputStream(s.getOutputStream());

            lightWeightsIn.put(s, sIn);
            lightWeightsOut.put(s, sOut);

            s.setSoTimeout(3000);
            try {
                // Ask for port
                System.out.println("Asking for port");
                sOut.writeChar('P');

                // Save the port
                int i = sIn.readInt();
                ports.add(i);
                System.out.println("port : " + i);
            } catch (Exception e) {it.remove(); continue;}
            s.setSoTimeout(0);
        }

        return ports;
    }


    /**
     * Signal to other heavy weight process that we're done using the resource
     */
    public static void sendTokenToHeavyweight() {
        try {
            token = 0;
            out.writeInt(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read the token sent by other heavy weight process
     */
    public static void listenHeavyweight() {
        try {
            token = in.readInt();
            System.out.println("Received: " + token);
        } catch (SocketTimeoutException timeout) {
            System.out.println("Timeout for socket " + socket.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Tell the lightweight that it can use the resource if they want
     */
    private static void sendActionToLightweight() {
        lightWeightsOut.forEach((socket, sOut) -> {
            try {
                sOut.writeChar('W');
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    
    // TODO implement this
    /**
     * Wait for all lightweight process to respond to sendActionToLightWeight()
     */
    private static void listenLightweight() {
        answersfromLightweigth = 0;

        lightWeightsOut.forEach((socket, sOut) -> {
            try {
                DataInputStream sIn = lightWeightsIn.get(socket);
                sOut.writeChar('D');

                Character c = sIn.readChar();
                if (c.equals('Y'))
                    answersfromLightweigth++;

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    public static void main(String[] args) throws IOException {
        System.out.println("----- HeavyProcess A -----");
        new HeavyProcessA();
        Boolean stop = false;

        //Enter the main loop
        while(true){
            while(token == 0)
                listenHeavyweight();

            sendActionToLightweight();
            answersfromLightweigth = 0;
            
            while (answersfromLightweigth < NUM_LIGHTWEIGHTS) {
                listenLightweight();
                System.out.println("Asking if the subprocesses are done...");
            }

            System.out.println("They are done");
            sendTokenToHeavyweight();
        }
    }
}
