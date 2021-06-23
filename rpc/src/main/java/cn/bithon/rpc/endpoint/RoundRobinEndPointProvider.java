package cn.bithon.rpc.endpoint;

import java.util.concurrent.ThreadLocalRandom;

public class RoundRobinEndPointProvider implements IEndPointProvider {
    final EndPoint[] endpoints;
    int index;

    public RoundRobinEndPointProvider(EndPoint... endpoints) {
        this.endpoints = endpoints;

        index = ThreadLocalRandom.current().nextInt(endpoints.length);
    }

    @Override
    public EndPoint getEndpoint() {
        return endpoints[index++ % endpoints.length];
    }
}
