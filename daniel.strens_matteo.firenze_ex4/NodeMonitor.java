import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NodeMonitor {
    private static final int NODE_PORT = 6969;
    private ServerSocket server;
    private ExecutorService executor;
    
    private HashMap<Socket, Integer> coreLayerNodes;
    private HashMap<Socket, Integer> layer1Nodes;
    private HashMap<Socket, Integer> layer2Nodes;


    public NodeMonitor() throws IOException, InterruptedException {
        this.server = new ServerSocket(NODE_PORT);
        this.executor = Executors.newFixedThreadPool(10);
        
        this.coreLayerNodes = new HashMap<>();
        this.layer1Nodes = new HashMap<>();
        this.layer2Nodes = new HashMap<>();

        // Loop to accept nodes
        executor.execute(() -> {
            while (true) {
                try {
                    Socket s = server.accept();
                    BufferedReader bf = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    
                    int layer = Integer.parseInt(bf.readLine());
                    int port = Integer.parseInt(bf.readLine());

                    switch (layer) {
                        case 0:
                            coreLayerNodes.put(s, port);
                            break;
                        
                        case 1:
                            layer1Nodes.put(s, port);
                            break;
                        
                        case 2:
                            layer2Nodes.put(s, port);
                            break;
                        default:
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        // Loop to get nodes data and display it
        executor.execute(() -> {
            while (true) {
                try {
                    displayNodeInfo();
                    Thread.sleep(1000); // Adjust the sleep duration as needed
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void displayNodeInfo() {
        // Clear console screen
        System.out.print("\033[H\033[2J");
        System.out.flush();
        String toDisplay = "";

        // Display information for each node
        for (int layer = 0; layer < 3; layer++) {
            toDisplay = toDisplay.concat("------ Layer " + layer + " ------\n");
            
            LinkedList<Socket> l = new LinkedList<>();
            switch (layer) {
                case 0:
                    l.addAll(coreLayerNodes.keySet());
                    break;

                case 1:
                    l.addAll(layer1Nodes.keySet());
                    break;
                case 2: 
                    l.addAll(layer2Nodes.keySet());
                    break;
            
                default:
                    break;
            }
            for (Socket node : l) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(node.getInputStream()));
                    DataOutputStream out = new DataOutputStream(node.getOutputStream());

                    // Send a command to the node to get information
                    out.writeChar(('G'));

                    // Receive node information
                    String buffer = "";
                    while (! (buffer=in.readLine()).equals("EOF")) {
                        toDisplay = toDisplay.concat(buffer + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        int nodes =  coreLayerNodes.size() + layer1Nodes.size() + layer2Nodes.size();
        System.out.println("Number of nodes : " + nodes);
        System.out.println(toDisplay);
    }

    public static void main(String[] args) throws Exception {
        NodeMonitor nodeMonitor = new NodeMonitor();
    }
}
