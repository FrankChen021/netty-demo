package com.sbss.bithon.component.brpc.endpoint;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class RoundRobinEndPointProvider implements IEndPointProvider {
    final EndPoint[] endpoints;
    int index;

    public RoundRobinEndPointProvider(EndPoint... endpoints) {
        this.endpoints = endpoints;

        index = ThreadLocalRandom.current().nextInt(endpoints.length);
    }

    public RoundRobinEndPointProvider(Collection<EndPoint> endpoints) {
        this(endpoints.toArray(new EndPoint[0]));
    }
    @Override
    public EndPoint getEndpoint() {
        return endpoints[index++ % endpoints.length];
    }
}
