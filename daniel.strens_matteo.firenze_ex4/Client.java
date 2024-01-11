import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Random;

public class Client {
    static private String transactionFilePath;

    static private LinkedList<Socket> coreLayer;
    static private LinkedList<Socket> layer1;
    static private LinkedList<Socket> layer2;


    /**
     * 
     * @param args file containing the transactions to execute,
     * followed by a list of the different nodes and their layer 
     * @throws IOException
     * @throws UnknownHostException
     * @throws NumberFormatException
     */
    public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {
        if (args.length < 3 || (args.length & 1) == 0)
            return;
        
        coreLayer = new LinkedList<>();
        layer1 = new LinkedList<>();
        layer2 = new LinkedList<>();
    
        transactionFilePath = args[0];

        // Populate the different Lists
        for (int i = 1; i < args.length; i++) {
            switch (Integer.parseInt(args[i+1])) {
                case 0:
                    coreLayer.add(new Socket("localhost", Integer.parseInt(args[i])));
                    break;
                
                case 1:
                    layer1.add(new Socket("localhost", Integer.parseInt(args[i])));
                    break;

                case 2:
                    layer2.add(new Socket("localhost", Integer.parseInt(args[i])));
                    break;
            
                default:
                    break;
            }
            i++;            
        }

        // Send the transactions 1 by 1
        sendTransactions(transactionFilePath);
    }

     public static void sendTransactions(String filePath) {
         try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            System.out.println("CLIENT going to send some stuff");
            String line;
            Socket sendTo = null;
            Random random = new Random();

            while ((line = br.readLine()) != null) {
                Thread.sleep(1000);
                System.out.println("Gonna send : " + line);

                // Non read-only transaction
                if (line.charAt(1) == ',') {
                    System.out.println("send to core layer");
                    int i = random.nextInt(coreLayer.size());
                    
                    PrintWriter pw = new PrintWriter(coreLayer.get(i).getOutputStream(), true);
                    BufferedReader bf = new BufferedReader(new InputStreamReader(coreLayer.get(i).getInputStream()));

                    pw.println(line);
                    System.out.println(bf.readLine());

                    continue;
                }

                // Read transaction on layer 1
                if (line.charAt(1) == '1') {
                    
                    int i = random.nextInt(layer1.size());

                    PrintWriter pw = new PrintWriter(layer1.get(i).getOutputStream(), true);
                    BufferedReader bf = new BufferedReader(new InputStreamReader(layer1.get(i).getInputStream()));

                    pw.println(line);
                    System.out.println(bf.readLine());

                    continue;
                }

                // Read transaction on layer 2
                if (line.charAt(1) == '2') {
                    int i = random.nextInt(layer2.size());

                    PrintWriter pw = new PrintWriter(layer2.get(i).getOutputStream(), true);
                    BufferedReader bf = new BufferedReader(new InputStreamReader(layer2.get(i).getInputStream()));
                    
                    pw.println(line);
                    System.out.println(bf.readLine());;
                    
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
