import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Node {
    private static final int SIZE = 100;
    private int[] data;
    private String logFilePath;
    private int layer;
    
    private int operationsDone;

    private ExecutorService executor;
    
    private ServerSocket serverForNodes;
    private NodeCommunication com;

    private ServerSocket serverForClients;
    private int localPort;
    
    private Node(int layer, int clientPort, int nodePort, String logFilePath) throws IOException {
        this.data = new int[SIZE];
        this.layer = layer;
        this.logFilePath = logFilePath;

        this.operationsDone = 0;

        this.serverForNodes = new ServerSocket(nodePort);
        this.localPort = nodePort;

        this.serverForClients = new ServerSocket(clientPort);

        // Create a thread pool
        this.executor = Executors.newFixedThreadPool(10);
    }
    
    /**
     * Loop that listens for new clients and handles them in a separate thread
     */
    private void listenForNewClients() {
        try {
            while (true) {
                Socket client = serverForClients.accept();
                executor.execute(() -> handleClient(client));
            }
        } catch (Exception e) {
            System.out.println("Client disconnected");
        }
    }

    /**
     * Connects to the parent node, brother nodes and children nodes
     * @param port the port of the parent node ServerSocket
     * @throws IOException
     * @throws UnknownHostException
     * @throws InterruptedException
     */
    private void initiateCommunications(int parentPort, int childrenCount, int mainProcess) throws UnknownHostException, IOException, InterruptedException {
        HashMap<Socket, Integer> layerMap = new HashMap<>();
        
        // Connect to parent
        if (this.layer > 0) {
            Socket parenSocket = new Socket("localhost", parentPort);
            DataOutputStream parentOutput = new DataOutputStream(parenSocket.getOutputStream());
            parentOutput.writeInt(this.layer);

            layerMap.put(parenSocket, this.layer-1);
        }

        // Connect to the mainProcess to get the different ports of this layer
        Socket mainSocket = new Socket("localhost", mainProcess);
        DataOutputStream mainOut = new DataOutputStream(mainSocket.getOutputStream());
        DataInputStream mainIn = new DataInputStream(mainSocket.getInputStream());

        mainOut.writeInt(this.layer);
        mainOut.writeInt(this.localPort);
        ArrayList<Integer> brotherPorts = new ArrayList<Integer>();
        int broSmallerPortCount = 0;
        while (true) {
            Integer broPort = mainIn.readInt();
            System.out.println("port " + localPort + " Received " + broPort);
            if (broPort.equals(0)) {
                System.out.println("port " + localPort + " Stop receiving ports");
                break;
            }
            brotherPorts.add(broPort);
            if (broPort < this.localPort) broSmallerPortCount++;
        }

        // Accept connections that will need to be sorted
        ArrayList<Socket> socketsToSort = new ArrayList<Socket>();
        System.out.println("port " + localPort + " Expecting " + (childrenCount+broSmallerPortCount) + " connections");
        for (int i = 0; i < (childrenCount + broSmallerPortCount); i++) {
            Socket socket = serverForNodes.accept();
            socketsToSort.add(socket);
        }

        // Sort connections
        for (Socket socket : socketsToSort) {
            DataInputStream socketIn = new DataInputStream(socket.getInputStream());
            int socketLayer = socketIn.readInt();
            layerMap.put(socket, socketLayer);
        }

        // Connect to the other nodes in this layer
        System.out.println("port " + localPort + " Ports of nodes in this layer : " + brotherPorts.toString());
        for (Integer port : brotherPorts) {
            if (port <= this.localPort) continue;
            System.out.println("port " + localPort + " connecting to " + port);
            Socket broSocket = new Socket("localhost", port);
            
            DataOutputStream broOut = new DataOutputStream(broSocket.getOutputStream());
            broOut.writeInt(this.layer);

            layerMap.put(broSocket, this.layer);
        }

        // Create the NodeCommunication object
        System.out.println("\nport " + localPort + " Creating the com object" +
        "\nLayer map : " + layerMap.toString());
        this.com = new NodeCommunication(this.layer, layerMap, localPort);

        System.out.println("\nport " + localPort + " Successfully connected");
        mainSocket.close();
    }

    /**
     * 
     * @param First layer. 
     * @param Second client port
     * @param Third port of the parent node (0 if there is none)
     * @param Fourth number of children to expect
     * @param Fifth port of the Main process
     * @param Sixth port for the com with other nodes
     * @param seventh the path to the log file
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 7) {
            return;
        }
        int layer = Integer.parseInt(args[0]);
        int clientPort = Integer.parseInt(args[1]);
        int parentPort = Integer.parseInt(args[2]);
        int childrenCount = Integer.parseInt(args[3]);
        int mainProcess = Integer.parseInt(args[4]);
        int nodePort = Integer.parseInt(args[5]);
        String file = args[6];

        Node node = new Node(layer, clientPort, nodePort, file);
        
        // Create the communications object
        node.initiateCommunications(parentPort, childrenCount, mainProcess);
        
        // Start accepting clients
        node.executor.execute(()-> node.listenForNewClients());
        
        System.out.println("------------------- TEST ------------------- ");

        // A thread that iterates over the pendingRequests in this.com
        node.executor.execute(() -> {
            try {
                node.handlePendingRequests();
            } catch (Exception e) {}
        });

        // A thread that checks the pending requests for layer 1, every 10 seconds.
        if (node.layer == 1) {
            node.executor.execute(() -> node.layer1TenSecondsUpdate());
        }

        //TODO connect to the webapp

    }

    private void handlePendingRequests() throws Exception {
        while (true) {
            Thread.sleep(250);
            synchronized(this.com.pendingRequests) {
                for (String s : this.com.pendingRequests.keySet()) {
                    if (s.equals("b, ack, c")) continue;
                    
                    Socket sender = this.com.pendingRequests.get(s);
                    writeTransaction(s);

                    this.com.pendingRequests.remove(s);
                    logVersion();
    
                    if (this.layer == 0)
                        this.com.sendToSocket("b, ack, c", sender);
                }
            }
        }
    }

    /**
     * Logs this version of the data in the specified file.
     */
    private void logVersion() {
        // Check if data and logFilePath are not null
        if (data == null || logFilePath == null) {
            System.out.println("Data or logFilePath is null. Cannot log version.");
            return;
        }

        // Create a formatted timestamp
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // Create a StringBuilder to build the log content
        StringBuilder logContent = new StringBuilder();

        // Append the timestamp to the log content
        logContent.append(timeStamp).append(" : [");

        // Append each element in the data array to the logContent
        for (int i = 0; i < data.length; i++) {
            logContent.append(i).append(": ").append(data[i]);
            if (i < data.length - 1) {
                logContent.append(", ");
            }
        }

        // Close the log entry
        logContent.append("]").append(System.lineSeparator());

        // Specify the file path using Paths
        Path filePath = Path.of(logFilePath);

        try {
            // Write the logContent to the file using Files.write
            Files.write(filePath, logContent.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            System.out.println("Log written to: " + logFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles interractions with a client
     * @param client the Socket on which the client is connected
     */
    private void handleClient(Socket client) {
        System.out.println("port " + this.localPort + " A client connected to this node");
        try {
            BufferedReader clientIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter clientOut = new PrintWriter(client.getOutputStream(), true);
            
            while (true) {
                String transaction = clientIn.readLine();
                System.out.println("port " + this.localPort + "Received from client : " + transaction);
                String response = new String();
                
                // Core layer Write op
                if (this.layer == 0) {
                    Pattern writeAndReadPattern = Pattern.compile("^b, ((r\\(\\d+\\), )|(w\\(\\d+,\\d+\\), ))*c$");
                    Matcher writeAndReadMatcher = writeAndReadPattern.matcher(transaction);
                    
                    if (writeAndReadMatcher.matches()) {
                        synchronized(data) {
                            writeTransaction(transaction);
                            System.out.println("Logging cause client made changes");
                            logVersion();

                            response = response.concat(prepareResults(transaction));

                            // Checks that the other nodes are done with the transaction
                            this.com.sendToAllInLayer(transaction);
                            
                            while (true) {
                                Thread.sleep(250);
                                synchronized (this.com.pendingRequests) {
                                    if(! (countOccurrences(this.com.pendingRequests.keySet(), "b, ack, c") < this.com.nodesInLayer))
                                        continue;
                                    
                                    for (int i = 0; i < this.com.nodesInLayer; i++)
                                        this.com.pendingRequests.remove("b, ack, c");
                                    break;
                                }
                            }

                            clientOut.println(response);
                        }

                        continue;
                    }
                }

                // Every layer read op
                Pattern readOnlyPattern = Pattern.compile("^b[012], (r\\(\\d+\\), )*c$");
                Matcher readOnlyMatcher = readOnlyPattern.matcher(transaction);

                if (readOnlyMatcher.matches()) {
                    response = response.concat(prepareResults(transaction));
                    clientOut.println(response);
                }
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Updates the data
     * @param transaction
     */
    private String writeTransaction(String transaction) {
        Pattern pairPattern = Pattern.compile("w\\((\\d+),(\\d+)\\),");
        Matcher pairMatcher = pairPattern.matcher(transaction);
        String cleanedTransaction = "";

        while (pairMatcher.find()) {
            cleanedTransaction = cleanedTransaction.concat(pairMatcher.group() + " ");
            String firstNumber = pairMatcher.group(1);
            String secondNumber = pairMatcher.group(2);
            int index = Integer.parseInt(firstNumber);
            int newValue = Integer.parseInt(secondNumber);

            // Update the data
            this.data[index] = newValue;
        }

        this.operationsDone++;
        System.out.println("port " + this.localPort + " Operations done : " + operationsDone);

        // Check if it's the tenth op (for next layer) and send updates if necessary
        if (this.layer == 0 && operationsDone%10 == 0) {
            String update = "b, ";
            for(int i = 0; i<SIZE; i++)
                update = update.concat("w(" + i + "," + data[i] + "), ");

            update = update.concat("c");
            this.com.sendToOuterLayer(update);
        }

        return cleanedTransaction;
    }

    /**
     * The method responsible for updating the layer 2 every 10 seconds
     */
    private void layer1TenSecondsUpdate() {
        while (true) {
            try {
                Thread.sleep(10_000);

                String update = "";
                for(int i = 0; i<SIZE; i++)
                    update = update.concat("w(" + i + "," + data[i] + "), ");

                this.com.sendToOuterLayer(update);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * prepares the results to send back to the client, based on the given transaction
     * @param transaction the transaction containing the read operations
     * @return A string containing the requested data
     */
    private String prepareResults(String transaction) {
        String response = new String("");
        Pattern readPattern = Pattern.compile("r\\((\\d+)\\)");
        Matcher readMatcher = readPattern.matcher(transaction);

        while (readMatcher.find()) {
            int index = Integer.parseInt(readMatcher.group(1));
            if (index < SIZE) {
                response = response.concat("[" + String.valueOf(index) + ": " + String.valueOf(this.data[index]) + "], ");
            }
        }

        return response;

    }
    
    /**
     * Counts the occurences of 'target' in 'list'
     * @param list the Collection in which to count occurences
     * @param target what we want to count
     * @return the number of times the target appears in the collection
     */
    private static int countOccurrences(Collection<String> list, String target) {
        int count = 0;

        for (String element : list) {
            if (element.equals(target)) {
                count++;
            }
        }

        return count;
    }
}
