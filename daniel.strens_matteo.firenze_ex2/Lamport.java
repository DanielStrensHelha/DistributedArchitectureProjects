import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Lamport implements IClockAlgorithm{
    private List<Thread> toClose;
    private Boolean shouldClose;
    private int portNumber;

    private HashMap<Socket, DataOutputStream> sOuts;
    private HashMap<Integer, Integer> timestampVector;
    private HashMap<Integer, Boolean> ackVector;
    private HashMap<Socket, Integer> ports;

    private LinkedHashMap<Integer, Integer> priorityQueue;
    private boolean wantToEnterCS;
    private DataOutputStream displayOut;

    /**
     * Constructor
     * @param brothers the sockets of communication to the other light weights
     * @param port This light weight's port number
     */
    Lamport(ArrayList<Socket> brothers, int port, List<Integer> brosPorts,DataOutputStream displayOut) {
        shouldClose = false;
        toClose = new ArrayList<Thread>();
        portNumber = port;
        ports = new HashMap<Socket, Integer>();
        this.sOuts = new HashMap<Socket, DataOutputStream>();
        
        wantToEnterCS = false;
        priorityQueue = new LinkedHashMap<Integer, Integer>();
        timestampVector = new HashMap<Integer, Integer>();
        ackVector = new HashMap<Integer, Boolean>();

        this.displayOut = displayOut;

        for (int it = 0; it < brothers.size(); it++) {
            Socket socket = brothers.get(it);
            int broPort = (brosPorts.get(it) >= this.portNumber) ? brosPorts.get(it+1) : brosPorts.get(it);

            this.ports.put(socket, broPort);
            this.timestampVector.put(broPort, 0);
            this.ackVector.put(broPort, false);
        }
        this.timestampVector.put(this.portNumber, 0);

        startListening();
    }

    @Override
    public void startListening() {
        this.ports.forEach((socket, i) -> {
            log("ports to write to \n\n" + ports.toString());
            Thread thLight  = new Thread(() -> {
                DataInputStream sIn = null;
                DataOutputStream sOut = null;
                try {
                    socket.setSoTimeout(3000);
                    sIn = new DataInputStream(socket.getInputStream());
                    sOut = new DataOutputStream(socket.getOutputStream());
                    sOuts.put(socket, sOut);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                while (true) {
                    try {
                        if (shouldClose) {
                            return;
                        }
                        listenLight(sIn, sOut, socket);
                    } 
                    catch (SocketTimeoutException e) {log("Exception : " + e.getMessage());}
                    catch (Exception e) {e.printStackTrace(); return;}
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
     * implement this
     * @param sIn
     * @throws IOException
     */
    private void listenLight(DataInputStream sIn, DataOutputStream sOut, Socket s) throws IOException {
        Character c = sIn.readChar();
        log("Received from LW " + ports.get(s) + ": " + c);
        
        int timestamp = 0;
        
        switch (c) {
            case 'R': // Request ?
                // Wait for the timestamp
                timestamp = sIn.readInt();
                manageTimestamp(timestamp, s);
                
                // Send acknowledgment
                synchronized (sOut) {
                    log("Sending back ACK");
                    sOut.writeChar('A');
                    sOut.writeInt(timestampVector.get(this.portNumber));
                }
                
                enqueue(ports.get(s), timestamp);
                break;

            case 'A': // Acknowledgement!
                // Wait for the timestamp
                timestamp = sIn.readInt();
                manageTimestamp(timestamp, s);

                if  (!this.wantToEnterCS)
                    break;
                // Set acknowledgment as received
                ackVector.put(ports.get(s), true);
                log("Ack Vector at this point : " + ackVector.toString());
                break;
            case 'D': // Done using the CS.
                // Wait for the timestamp
                timestamp = sIn.readInt();
                manageTimestamp(timestamp, s);

                if (priorityQueue.containsKey(ports.get(s)))
                    priorityQueue.remove(ports.get(s));
                break;

            default:
                break;
        }
    }
    
    /**
     * Updates the timestamp vector and prints it
     * @param timestamp
     * @param s
     */
    private void manageTimestamp(int timestamp, Socket s) {
        timestampVector.put(ports.get(s), timestamp);
        timestampVector.put(this.portNumber, 1 + Integer.max(timestamp, timestampVector.get(this.portNumber)));
        System.out.println("Timestamp update : " + timestamp + " for " + ports.get(s) + "\n" + timestampVector);
    }

    @Override
    public void writeToResource() throws InterruptedException {
        this.wantToEnterCS = true;
        click();

        // Enqueue request
        enqueue(this.portNumber, timestampVector.get(this.portNumber));

        // Send request to everyone
        log("Sending request to : " + sOuts.toString());
        sOuts.forEach((s, sOut) -> {
            try {sOut.writeChar('R'); sOut.writeInt(timestampVector.get(this.portNumber));} 
            catch (IOException e) {e.printStackTrace();}
        });

        // Wait for ack's
        while (ackVector.containsValue(false))
            Thread.sleep(500);

        // Wait to be at the top of the queue
        while (priorityQueue.keySet().iterator().next() != this.portNumber)
            Thread.sleep(500);

        // Enter the CS and write to display
        for(int i = 0; i<10; i++) {
            writeToDisplay("I'm (Lamport, port : " + this.portNumber + ") writing to the console, messange : " + i);
            Thread.sleep(100);
        }
        this.wantToEnterCS = false;
        sOuts.forEach((s, sOut) -> {
            try {
                sOut.writeChar('D');
                sOut.writeInt(timestampVector.get(this.portNumber));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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

    /**
     * Enqueue the request
     * @param port the ID of the requesting process
     * @param timestamp the timestamp with which to order the queue
     */
    private void enqueue(Integer port, int timestamp) {
        // Add to the PQ and resort it
        synchronized (priorityQueue) {
            priorityQueue.put(port, timestamp);
            priorityQueue = priorityQueue.entrySet().stream()
                .sorted((Map.Entry.<Integer, Integer>comparingByValue()
                .thenComparing(Map.Entry.comparingByKey())))
                .collect(Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)
                );
        }
    }

    private void click() {
        timestampVector.put(this.portNumber, 1 + timestampVector.get(this.portNumber));
    }

    /**
     * Write to the console, adds the server port to recognize which sub process is writing.
     * @param s
     */
    private void log(String s) {
        System.out.println("[Port:" + this.portNumber + "] > " + s);
    }
}