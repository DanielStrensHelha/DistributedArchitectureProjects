import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    private static final int LOCAL_PORT = 5000;
    private static final String NODE_PROCESS = "Node";
    private static final String CLIENT_PROCESS = "Client";

    private static HashMap<Socket, Integer> socketLayerMap;
    private static HashMap<Socket, Integer> socketPortMap;
    private static ServerSocket server;

    public static void main(String[] args) throws Exception {
        server = new ServerSocket(LOCAL_PORT);
        spawnProcesses();

        System.out.println("------------ All processes have been created and interconnected among the layers ------------");

        // Spawn the clients
        Thread.sleep(3_000);
        
        String[] arguments = new String[socketLayerMap.size() * 2 + 6];
        arguments[0] = "cmd";
        arguments[1] = "/c";
        arguments[2] = "start";
        arguments[3] = "java";
        arguments[4] = CLIENT_PROCESS;
        arguments[5] = "..\\Transactions1.txt";

        int i = 6;
        for (Socket socket : socketPortMap.keySet()) {
            arguments[i++] = String.valueOf(socketPortMap.get(socket) + 3000);
            arguments[i++] = String.valueOf(socketLayerMap.get(socket));
        }

        try {
            ProcessBuilder pBuilder = new ProcessBuilder(arguments).inheritIO();
            pBuilder.start();

            arguments[5] = "..\\Transactions2.txt";
            pBuilder = new ProcessBuilder(arguments).inheritIO();
            pBuilder.start();

            
            arguments[5] = "..\\Transactions3.txt";
            pBuilder = new ProcessBuilder(arguments).inheritIO();
            pBuilder.start();


            System.out.println("--------------- CLIENTS CREATED ---------------");
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
         * DEBUG ZONE
         */
        // Thread.sleep(1000);
        // Acting as a client to check if we can get data and update it
        // Socket s = new Socket("localhost", 10_001);
        
        // BufferedReader bf = new BufferedReader(new InputStreamReader(s.getInputStream()));
        // PrintWriter pw = new PrintWriter(s.getOutputStream(), true);

        // pw.println("b, w(20,5), r(0), r(1), c");
        // System.out.println(bf.readLine());
        
        //     Thread.sleep(150);

        // pw.println("b, w(0,5), r(0), r(1), c");
        // System.out.println(bf.readLine());
        
        //     Thread.sleep(150);

        // pw.println("b, w(0,5), r(0), r(1), c");
        // System.out.println(bf.readLine());
        
        //     Thread.sleep(150);

        // pw.println("b, w(0,5), r(0), r(1), c");
        // System.out.println(bf.readLine());
        
        //     Thread.sleep(150);

        // pw.println("b, w(0,5), r(0), r(1), c");
        // System.out.println(bf.readLine());
        
        //     Thread.sleep(150);

        // pw.println("b, w(0,5), r(0), r(1), c");
        // System.out.println(bf.readLine());
        
        //     Thread.sleep(150);
        // pw.println("b, w(0,12), w(1,5), r(1), c");
        // System.out.println(bf.readLine());
        
        //     Thread.sleep(150);
        // pw.println("b, w(2,5), w(0,87), w(9,6), c");
        // System.out.println(bf.readLine());
        
        //     Thread.sleep(150);
        // pw.println("b, w(2,67), w(3,87), w(6,6), c");
        // System.out.println(bf.readLine());
        
        //     Thread.sleep(150);
        // pw.println("b, w(0,5), r(0), r(1), c");
        // System.out.println(bf.readLine());
        
        //     Thread.sleep(150);
        // pw.println("b, w(0,9), r(0), r(1), c");
        // System.out.println(bf.readLine());

        // while (true)
        //     Thread.sleep(10_000);
         /*
          * END OF DEBUG ZONE
          */
    }

    /**
     * Spawns the processes and connects them in their layers.
     */
    private static void spawnProcesses() {
        /*
        * Arguments in the process building
        * First layer. 
        * Second client port
        * Third port of the parent node (0 if there is none)
        * Fourth number of children to expect
        * Fifth port of the Main process
        * Sixth port for the com with other nodes
        */
        socketLayerMap = new HashMap<>();
        socketPortMap = new HashMap<>();
        ArrayList<ProcessBuilder> processes = new ArrayList<ProcessBuilder>();
        ProcessBuilder pBuilder = null;
        
        // creating the processes
        try {
            // Core layer
            pBuilder = new ProcessBuilder("java",  
                NODE_PROCESS,
                "0", "10001", "0", "0", String.valueOf(LOCAL_PORT), "7001", "logFileA1.txt").inheritIO();
            processes.add(pBuilder);
            System.out.println("Node A1 created");
            
            pBuilder = new ProcessBuilder("java",  
            NODE_PROCESS,
            "0", "10002", "0", "1", String.valueOf(LOCAL_PORT), "7002", "logFileA2.txt").inheritIO();
            processes.add(pBuilder);
            System.out.println("Node A2 created");
            
            pBuilder = new ProcessBuilder("java",  
                NODE_PROCESS,
                "0", "10003", "0", "1", String.valueOf(LOCAL_PORT), "7003", "logFileA3.txt").inheritIO();
            processes.add(pBuilder);
            System.out.println("Node A3 created");

            // Layer 1
            pBuilder = new ProcessBuilder("java",  
                NODE_PROCESS,
                "1", "11001", "7002", "0", String.valueOf(LOCAL_PORT), "8001", "logFileB1.txt").inheritIO();
            processes.add(pBuilder);
            System.out.println("Node B1 created");

            pBuilder = new ProcessBuilder("java",  
                NODE_PROCESS,
                "1", "11002", "7003", "2", String.valueOf(LOCAL_PORT), "8002", "logFileB2.txt").inheritIO();
            processes.add(pBuilder);
            System.out.println("Node B2 created");
            
            // Layer 2
            pBuilder = new ProcessBuilder("java",  
                NODE_PROCESS,
                "2", "12001", "8002", "0", String.valueOf(LOCAL_PORT), "9001", "logFileC1.txt").inheritIO();
            processes.add(pBuilder);
            System.out.println("Node C1 created");

            pBuilder = new ProcessBuilder("java",  
                NODE_PROCESS,
                "2", "12002", "8002", "0", String.valueOf(LOCAL_PORT), "9002", "logFileC2.txt").inheritIO();
            processes.add(pBuilder);
            System.out.println("Node C2 created");

            // Starting the processes
            processes.forEach(process -> {try {
                System.out.println("Starting the processes...");
                process.start();
            } catch (IOException e) {
                e.printStackTrace();
            }});

            // Getting the port and layer of each node
            for(int i=0; i < processes.size(); i++) {
                Socket nodeSocket = server.accept();
                DataInputStream in = new DataInputStream(nodeSocket.getInputStream());
                
                int layer = in.readInt();
                int port = in.readInt();
                System.out.println("Received port " + port + " for layer " + layer);
                socketPortMap.put(nodeSocket, port);
                socketLayerMap.put(nodeSocket, layer);
            }

            // Send the ports to connect the nodes in their layer
            System.out.println("--- Layer map ---\n" + socketLayerMap + "\n\n--- Port map ---\n" + socketPortMap);
            
            socketLayerMap.forEach((socket, layer) -> {try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                socketLayerMap.forEach((s, l) -> {
                    if (l == layer && l==0) {
                        try {
                            out.writeInt(socketPortMap.get(s));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                out.writeInt(0);
            } catch (Exception e){e.printStackTrace();}});
            return;
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
