import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.websockets.*;

import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebappLink {
    private static int port;
    private static int layer;
    private static int[] data;

    private int websocketPort;

    public WebappLink(int port, int layer, int[] data, int webSocketPort) {
        WebappLink.port = port;
        WebappLink.layer = layer;
        this.websocketPort = webSocketPort;
        WebappLink.data = data;

        runWebSocketServer();
    }

    public void runWebSocketServer() {
        System.out.println("REACHES HERE");

        // Initialize Grizzly server
        HttpServer httpServer = new HttpServer();
        ServerConfiguration config = httpServer.getServerConfiguration();

        // Add WebSocket support to the server
        WebSocketServerFilter wsFilter = new WebSocketServerFilter();
        wsFilter.addWebSocketApplication("/websocket", new WebSocketEndpointConfig());

        // Add the WebSocket filter to the server
        config.addHttpHandler(wsFilter, "/");

        try {
            // Start the server
            httpServer.start();

            System.out.println("WebSocket Server running...");
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpServer.shutdownNow();
        }
    }

    public static class WebSocketEndpointConfig extends ServerEndpointConfig.Configurator {
        @Override
        public <T extends Endpoint> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
            WebSocketEndpoint endpoint = new WebSocketEndpoint();
            endpoint.setLayer(layer);
            endpoint.setPort(port);
            endpoint.setData(data);
            return (T) endpoint;
        }
    }

    public static class WebSocketEndpoint extends Endpoint {
        private int layer;
        private int port;
        private int[] data;

        public void setLayer(int layer) {
            this.layer = layer;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void setData(int[] data) {
            this.data = data;
        }

        @Override
        public void onOpen(Session session, EndpointConfig config) {
            System.out.println("WebSocket Connection opened");

            // Access layer, port, and data here
            System.out.println("Layer: " + layer + ", Port: " + port);
            for (int d : data) {
                System.out.println("Data: " + d);
            }

            // Schedule a task to send messages every 3 seconds
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(() -> {
                try {
                    sendMessage(session);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, 0, 3, TimeUnit.SECONDS);
        }

        @Override
        public void onClose(Session session, CloseReason closeReason) {
            System.out.println("WebSocket Connection closed");
        }

        private void sendMessage(Session session) throws IOException {
            // Construct the message using the values of port, layer, and data
            String message = "Port: " + port + ", Layer: " + layer + ", Data: " + arrayToString(data);

            // Send the message to the client
            session.getBasicRemote().sendText(message);
        }

        private String arrayToString(int[] array) {
            StringBuilder builder = new StringBuilder("[");
            for (int i = 0; i < array.length; i++) {
                builder.append(array[i]);
                if (i < array.length - 1) {
                    builder.append(", ");
                }
            }
            builder.append("]");
            return builder.toString();
        }
    }
}
