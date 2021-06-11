import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private Map<String, ClientConnection> connections = new ConcurrentHashMap<>();

    public Map<String, ClientConnection> getConnections() {
        return connections;
    }
}
