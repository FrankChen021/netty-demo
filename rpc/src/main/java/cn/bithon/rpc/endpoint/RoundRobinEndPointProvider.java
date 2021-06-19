package cn.bithon.rpc.endpoint;

import java.util.concurrent.ThreadLocalRandom;

public class RoundRobinEndPointProvider implements IEndPointProvider {
    final Endpoint[] endpoints;
    int index;

    public RoundRobinEndPointProvider(Endpoint... endpoints) {
        this.endpoints = endpoints;

        index = ThreadLocalRandom.current().nextInt(endpoints.length);
    }

    @Override
    public Endpoint getEndpoint() {
        return endpoints[index++ % endpoints.length];
    }
}
