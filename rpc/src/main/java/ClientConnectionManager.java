import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientConnectionManager {

    private Map<String, ClientConnection> connections = new ConcurrentHashMap<>();

    public Map<String, ClientConnection> getConnections() {
        return connections;
    }
}
