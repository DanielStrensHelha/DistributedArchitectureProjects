import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class Agrawala implements IClockAlgorithm{
    private List<Thread> toClose;
    private Boolean shouldClose;
    private int portNumber;

    private HashMap<Socket, DataOutputStream> sOuts;
    private HashMap<Integer, Integer> timestampVector;
    private HashMap<Integer, Boolean> ackVector;
    private HashMap<Socket, Integer> ports;
    private HashMap<Integer, Integer> timestampRequestVector;

    private HashSet<Integer> priorityQueue;
    private boolean wantToEnterCS;
    private DataOutputStream displayOut;
    Agrawala(ArrayList<Socket> brothers, int port, List<Integer> brosPorts,DataOutputStream displayOut) {
        shouldClose = false;
        toClose = new ArrayList<Thread>();
        portNumber = port;
        ports = new HashMap<Socket, Integer>();
        this.sOuts = new HashMap<Socket, DataOutputStream>();
        
        this.wantToEnterCS = false;
        this.priorityQueue = new HashSet<Integer>();
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
        // Initialize timestampRequestVector for each port
         for (int port1 : ports.values()) {
             timestampRequestVector.put(port1, 0);
         }
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
        int timestamp = in.readInt();
        switch (c) {
            case 'R': // request, a process want to access the CS
            int timestampRequest = in.readInt();
            log("timestamp request : "+timestampRequest + " timestamp actuel : "+timestamp);
                this.timestampRequestVector.put(ports.get(socketEnvoie), timestampRequest);
                    if(timestamp <= this.timestampVector.get(this.portNumber))
                        click();
                    else this.timestampVector.put(this.portNumber, timestamp + 1);
                
                    if(!this.wantToEnterCS){
                        synchronized(out){
                            out.writeChar('A');
                            out.writeInt(timestamp);
                            out.flush();
                            log(this.portNumber +" send ack to "+ports.get(socketEnvoie)+" bc "+this.portNumber+"dont want CS");
                        }
                        return;
                    }

                    if(this.wantToEnterCS && this.timestampRequestVector.get(this.portNumber) == this.timestampRequestVector.get(ports.get(socketEnvoie))){
                        if(this.portNumber < ports.get(socketEnvoie)){
                            log(this.timestampRequestVector.get(this.portNumber)+" == "+this.timestampRequestVector.get(ports.get(socketEnvoie))+"   "+this.portNumber+" < "+ports.get(socketEnvoie)+" so "+this.portNumber+" enqueu "+ports.get(socketEnvoie));
                            enqueue(ports.get(socketEnvoie));
                        }
                        else{
                            synchronized(out){
                            out.writeChar('A');
                            out.writeInt(timestamp);
                            out.flush();
                            log(this.portNumber +" send ack to "+ports.get(socketEnvoie)+" bc "+ this.portNumber+" > "+ports.get(socketEnvoie));
                            } 
                        }
                    }

                    else if(this.wantToEnterCS && this.timestampRequestVector.get(ports.get(socketEnvoie)) < this.timestampRequestVector.get(this.portNumber)){
                            synchronized(out){
                                log(this.timestampRequestVector.get(ports.get(socketEnvoie)) +" < "+ this.timestampRequestVector.get(this.portNumber) + " so "+ this.portNumber +" send ack to " + ports.get(socketEnvoie));
                                out.writeChar('A');
                                out.writeInt(timestamp);
                                out.flush();
                            }
                        } else{
                            log( this.timestampRequestVector.get(ports.get(socketEnvoie)) + " > "+this.timestampRequestVector.get(this.portNumber)+" so "+this.portNumber+" enqueu "+ports.get(socketEnvoie));
                            enqueue(ports.get(socketEnvoie));
                        } 
                break;

                    
            case 'A': // Acknoledgement
        
                log("ack recu");
                    if(timestamp <= this.timestampVector.get(this.portNumber))
                        click();
                    else this.timestampVector.put(this.portNumber, timestamp + 1);
                    
                    this.ackVector.put(ports.get(socketEnvoie), true);
                    log("A  "+this.ackVector.toString());  
                
            break;
            
            default:
            break;
        }
    }
            /**
             * Write to the console, adds the server port to recognize which sub process is writing.
             * @param s
     */
    private  void log(String s) {
        System.out.println("[Port:" + this.portNumber + "] > " + s);
    }

    /**
     * Enqueue the request
     * @param port the ID of the requesting process
     * @param timestamp the timestamp with which to order the queue
     */
    private  void enqueue(Integer port) {
        // Add to the PQ and resort it
        synchronized (this.priorityQueue) {
            this.priorityQueue.add(port);
            log("PQ at this point : " + priorityQueue.toString());
        }
    }

     /**
     * Write to the display entity
     * @param s
     */
    private  void writeToDisplay(String s) {
        try {
            displayOut.writeChars(s + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private  void click() {
        timestampVector.put(this.portNumber, 1 + timestampVector.get(this.portNumber));
    }

    @Override
    public void writeToResource() throws InterruptedException {
            this.wantToEnterCS = true;
            click();
            synchronized(timestampVector){
                    timestampRequestVector.put(this.portNumber,timestampVector.get(this.portNumber));

                    // Send request to everyone
                    log("Sending request to : " + sOuts.toString());
                    sOuts.forEach((s, sOut) -> {
                        log("sending 'R' to " + ports.get(s));
                        try {
                            sOut.writeChar('R'); 
                            sOut.writeInt(timestampVector.get(this.portNumber));     
                            sOut.writeInt(timestampRequestVector.get(this.portNumber));     
                        } catch (IOException e) {e.printStackTrace();}   
                    });
                }

            // Wait for ack's
            log("PQ : " + this.priorityQueue.toString());
            while (ackVector.containsValue(false)) {
                Thread.sleep(500);
                log("Now checking ack... " + ackVector.toString());
            }
   
           // Enter the CS and write to display
           for(int i = 0; i<10; i++) {
               writeToDisplay("I'm (agrawala, port : " + this.portNumber + ") writing to the console, messange : " + i);
               Thread.sleep(100);
           }
           this.wantToEnterCS = false;
   
           //send A message to the one in the queue
           sOuts.forEach((s, sOut) -> {
               try {
                   if (this.priorityQueue.contains(ports.get(s))) {
                       ports.get(s);
                       sOut.writeChar('A');
                       sOut.writeInt(timestampVector.get(this.portNumber));
                       log("J'envoie l'ack de fin aux autres");
                   }
               } catch (IOException e) {
                   e.printStackTrace();
               }
        
        });
        log(timestampVector.get(this.portNumber)+"");
    }
    
}
