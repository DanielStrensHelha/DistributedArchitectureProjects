import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;


public class HeavyProcessB {
    private static ServerSocket serverSocket;
    private static ServerSocket server;
    private static Socket socket;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static int token = 0;
    private static int answersfromLightweigth;
    private static HashMap<Socket, DataOutputStream> lightWeightsOut;
    private static HashMap<Socket, DataInputStream> lightWeightsIn;
    private final static String SUB_PROCESS = "LightWeightProcess";
    private final static int LIGHT_WEIGHT_PORT = 5002;
    private final static int NUM_LIGHTWEIGHTS = 3;
    public HeavyProcessB() throws IOException {
        try {
            answersfromLightweigth = 0;
            lightWeightsOut = new HashMap<Socket, DataOutputStream>();
            lightWeightsIn = new HashMap<Socket, DataInputStream>();
            
            //Waiting for HeavyProcess A to connect
            serverSocket = new ServerSocket(5000);
            server = new ServerSocket(LIGHT_WEIGHT_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        createProcesses();
    }

    public static void startListening() {
        try {
            System.out.println("Waiting for other process to connect...");
            socket = serverSocket.accept();
            System.out.println("Connected");
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void receiveTokenFromHeavyweight() {
        try {
            token = in.readInt();
            System.out.println("Received: " + token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendHeavyweight() {
        try {
            out.writeInt(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void listenHeavyweight() throws IOException {
        token = in.readInt();
        System.out.println("Received: " + token);
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
                    Integer.toString(LIGHT_WEIGHT_PORT), "B").inheritIO();
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
        final List<Integer> ports = getPorts();
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
    private List<Integer> getPorts() throws IOException {
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
        final List<Integer> sortedPorts = new ArrayList<>(ports);
        Collections.sort(sortedPorts);
        return sortedPorts;
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
                if (c.equals('Y')) {
                    System.out.println("Received " + c);
                    answersfromLightweigth++;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    public static void main(String[] args) throws IOException {
        try {
            System.out.println("----- HeavyProcess B -----");
        new HeavyProcessB();
        Scanner scan = new Scanner(System.in);

        //Enter the main loop
        while(true){
            startListening();
            while(token == 0)
                listenHeavyweight();

            sendActionToLightweight();
            
            do {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}

                listenLightweight();

                System.out.println("Answers to if they're done : " + answersfromLightweigth);
            } while (answersfromLightweigth < NUM_LIGHTWEIGHTS);

            System.out.println("They are done");
            sendTokenToHeavyweight();
            
            System.out.println("press q <enter> to quit, anything else to continue");
            String s = scan.nextLine();
            
            if (s.equals("q"))
                break;
        }

        // Tell all subprocesses to die
        lightWeightsOut.forEach((s, sOut) -> {
            try {
                sOut.writeChar('/');
            } catch (IOException e) {}
        });

        scan.nextLine();
        scan.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
       
    }
}
