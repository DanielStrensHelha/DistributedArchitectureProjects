import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ServerEndpoint("/broadcast")
public class BroadcastServer {

    private Session session;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        this.scheduler.scheduleAtFixedRate(() -> sendData(), 0, 3, TimeUnit.SECONDS);
    }

    private void sendData() {
        int[] data = {1, 2, 3, 4, 5};
        try {
            this.session.getBasicRemote().sendText(java.util.Arrays.toString(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
