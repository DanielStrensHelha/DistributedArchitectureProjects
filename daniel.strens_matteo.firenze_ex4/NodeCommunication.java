import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NodeCommunication {
    private int layer;
    private int localPort;
    
    private HashMap<Socket, Integer> layerMap;
    private HashMap<Socket, BufferedReader> inputMap;
    private HashMap<Socket, PrintWriter> outputMap;
    private ExecutorService executor;
    
    public int nodesInLayer;
    public int nodesInOuterLayer;
    public LinkedHashMap<String, Socket> pendingRequests;

    public NodeCommunication(int layer, HashMap<Socket, Integer> layerMap, int localPort) throws InterruptedException {
        this.layer = layer;
        this.nodesInLayer = 0;
        this.nodesInOuterLayer = 0;
        
        this.layerMap = layerMap;
        this.localPort = localPort;

        this.pendingRequests = new LinkedHashMap<>();
        
        this.inputMap = new HashMap<Socket, BufferedReader>();
        this.outputMap = new HashMap<Socket, PrintWriter>();

        // Create a thread pool
        this.executor = Executors.newFixedThreadPool(10);

        // Create all BufferReader and PrintReaders
        for (Map.Entry<Socket, Integer> entry : layerMap.entrySet()) {
            Socket socket = entry.getKey();
            Integer l = entry.getValue();

            if (l == this.layer) nodesInLayer++;
            if (l > this.layer) nodesInOuterLayer++;

            try {
                inputMap.put(socket, new BufferedReader(new InputStreamReader(socket.getInputStream())));
                outputMap.put(socket, new PrintWriter(socket.getOutputStream(), true));
            } catch (Exception e) {
                System.out.println("\t" + "port " + this.localPort + "Something went wrong with " + socket.toString());
                this.layerMap.remove(socket);
                this.inputMap.remove(socket);
                this.outputMap.remove(socket);
                e.printStackTrace();
            }
        }

        // Start listening for requests
        startListening();

        // Check that we are successfully connected to all nodes provided
        for (Map.Entry<Socket, Integer> entry : layerMap.entrySet()) {
            Socket socket = entry.getKey();
            Integer l = entry.getValue();

            System.out.println("\t" + "port " + this.localPort + "," + " Sending 'bc' to " + l);
            outputMap.get(socket).println("bc");

            while (!pendingRequests.keySet().contains("b!c")) {
                Thread.sleep(75);
                // System.out.println("port " + localPort + "retrying...");
            }
            pendingRequests.remove("b!c");
        }
        System.out.println("\t" + "port " + this.localPort + " Received every reply necessary");
    }

    /**
     * Send a request to all nodes in this layer
     * @param request the request to send
     */
    public void sendToAllInLayer(String request) {
        for (Socket socket : layerMap.keySet()) {
            if (layerMap.get(socket) == this.layer)
                outputMap.get(socket).println(request);
        }
    }

    /**
     * Send a request to the parent node
     * @param request
     */
    public void sendToInnerLayer(String request) {
        for (Socket socket : layerMap.keySet()) {
            if (layerMap.get(socket) == this.layer - 1) {
                outputMap.get(socket).println(request);
                return;
            }
        }
    }

    /**
     * send a request to all nodes in the following layer
     * @param request the request to send
     */
    public void sendToOuterLayer(String request) {
        for (Socket socket : layerMap.keySet()) {
            if (layerMap.get(socket) == this.layer + 1)
                outputMap.get(socket).println(request);
        }
    }

    /**
     * Creates the different threads necessary to listen to all adjacent nodes
     */
    private void startListening() {
        for (Socket socket : inputMap.keySet()) {
            executor.execute(() -> {
                try {
                    String request;
                    while (true) {
                        request = inputMap.get(socket).readLine();
                        if (request == null)
                            break;
                        System.out.println("Received request : " + request);
                        processRequest(request, socket);
                    }
                    System.out.println("\t" + "port " + this.localPort + "Closing thread");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Puts the request in the pendingRequests if necessary
     * @param request the request to process
     * @param sender where it comes from
     */
    private void processRequest(String request, Socket sender) {
        if (request.equals("bc")) {
            outputMap.get(sender).println("b!c");
            return;
        }

        synchronized (pendingRequests) {
            pendingRequests.put(request, sender);
        }
    }

    public void sendToSocket(String message, Socket s) {
        outputMap.get(s).println(message);
    }
}
