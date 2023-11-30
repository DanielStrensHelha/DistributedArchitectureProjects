import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Agrawala implements IClockAlgorithm{
    private List<Thread> toClose;
    private Boolean shouldClose;
    private int portNumber;

    private HashMap<Socket, DataOutputStream> sOuts;
    private HashMap<Integer, Integer> timestampVector;
    private HashMap<Integer, Boolean> ackVector;
    private HashMap<Socket, Integer> ports;
    private HashMap<Integer, Integer> timestampRequestVector;

    private LinkedHashMap<Integer, Integer> priorityQueue;
    private boolean wantToEnterCS;
    private DataOutputStream displayOut;
    Agrawala(ArrayList<Socket> brothers, int port, List<Integer> brosPorts,DataOutputStream displayOut) {
        shouldClose = false;
        toClose = new ArrayList<Thread>();
        portNumber = port;
        ports = new HashMap<Socket, Integer>();
        this.sOuts = new HashMap<Socket, DataOutputStream>();
        
        wantToEnterCS = false;
        priorityQueue = new LinkedHashMap<Integer, Integer>();
        timestampVector = new HashMap<Integer, Integer>();
        ackVector = new HashMap<Integer, Boolean>();
        timestampRequestVector = new HashMap<Integer, Integer>();
        this.displayOut = displayOut;

        for (int i = 0; i < brothers.size(); i++) {
            Socket socket = brothers.get(i);
            int broPort = (brosPorts.get(i) >= this.portNumber) ? brosPorts.get(i+1) : brosPorts.get(i);

            this.ports.put(socket, broPort);
            this.timestampVector.put(broPort, 0);
            this.ackVector.put(broPort, false);
        }
        this.timestampVector.put(this.portNumber, 0);

        startListening();
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

    @Override
    public void startListening() {
        this.ports.forEach((socket,i)->{
            Thread thLight = new Thread(()->{
                DataInputStream in = null;
                DataOutputStream out = null;
                try{
                    in = new DataInputStream(socket.getInputStream());
                    out = new DataOutputStream(socket.getOutputStream());
                    sOuts.put(socket, out);
                } catch(Exception e){
                    e.printStackTrace();
                }

                while (true) {
                    try{
                        if(shouldClose){
                            return;
                        }
                        listenLight(in,out,socket);
                    } catch(Exception e){
                        e.printStackTrace();
                        return;
                    }
                }
            });
            thLight.start();
            toClose.add(thLight);
        });
    }

    private void listenLight(DataInputStream in, DataOutputStream out, Socket socketEnvoie)throws IOException {
        Character c = in.readChar();

        switch (c) {
            case 'R': // request, a process want to access the CS
                int timestamp = in.readInt();
                this.timestampVector.put(this.portNumber, timestamp + 1);
                //if we don't want to access the CS we send ACK
                if(!this.wantToEnterCS){
                    synchronized(out){
                        log("Sending back ACK");
                        out.writeChar('A');
                    } 
                    //if we want to enter and the timestamp request we received is smaller than our
                } else if(this.wantToEnterCS && (timestampRequestVector.get(ports.get(socketEnvoie)) < timestamp)){
                    enqueue(ports.get(socketEnvoie), timestamp);
                } else {
                    synchronized(out){
                        log("Sending back ACK");
                        out.writeChar('A');
                    }
                }
                

            break;
            case 'A': // Acknoledgement
                if(!this.wantToEnterCS)
                    break;
                ackVector.put(ports.get(socketEnvoie), true);
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
            log("PQ at this point : " + priorityQueue.toString());
        }
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

    private void click() {
        timestampVector.put(this.portNumber, 1 + timestampVector.get(this.portNumber));
    }

    @Override
    public void writeToResource() throws InterruptedException {
        this.wantToEnterCS = true;
        click();
        timestampRequestVector.put(this.portNumber,timestampVector.get(this.portNumber));
        // Send request to everyone
        log("Sending request to : " + sOuts.toString());
        sOuts.forEach((s, sOut) -> {
            log("sending 'R' to " + ports.get(s));
            try {
             sOut.writeChar('R'); 
             sOut.writeInt(timestampVector.get(this.portNumber));
            } catch (IOException e) {e.printStackTrace();}
            
        });

         // Wait for ack's
         log("PQ : " + priorityQueue.toString());
         while (ackVector.containsValue(false)) {
             Thread.sleep(500);
             log("Now checking ack... " + ackVector.toString());
         }
          // Wait to be at the top of the queue
        /*while (priorityQueue.keySet().iterator().next() != this.portNumber)
        Thread.sleep(500);*/

        // Enter the CS and write to display
        for(int i = 0; i<10; i++) {
            writeToDisplay("I'm (agrawala, port : " + this.portNumber + ") writing to the console, messange : " + i);
            Thread.sleep(1000);
        }
        this.wantToEnterCS = false;

        //send D message to the one in the queue
        sOuts.forEach((s, sOut) -> {
            try {
                if (priorityQueue.containsKey(ports.get(s))) {
                    ports.get(s);
                    sOut.writeChar('A');
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    
}
