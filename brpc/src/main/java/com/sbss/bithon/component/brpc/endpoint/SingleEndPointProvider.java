package com.sbss.bithon.component.brpc.endpoint;

public class SingleEndPointProvider implements IEndPointProvider {
    private final EndPoint ep;
    private final String host;
    private final int port;

    public SingleEndPointProvider(String host, int port) {
        this.host = host;
        this.port = port;
        ep = new EndPoint(host, port);
    }

    @Override
    public EndPoint getEndpoint() {
        return ep;
    }
}
