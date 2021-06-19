package cn.bithon.rpc.core.endpoint;

public class SingleEndPointProvider implements IEndPointProvider {
    private final Endpoint ep;
    private final String host;
    private final int port;

    public SingleEndPointProvider(String host, int port) {
        this.host = host;
        this.port = port;
        ep = new Endpoint(host, port);
    }

    @Override
    public Endpoint getEndpoint() {
        return ep;
    }
}
