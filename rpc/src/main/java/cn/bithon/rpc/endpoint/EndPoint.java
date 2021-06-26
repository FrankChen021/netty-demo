package cn.bithon.rpc.endpoint;

import java.net.InetSocketAddress;
import java.util.Objects;

public class EndPoint {
    private final String host;
    private final int port;

    public EndPoint(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static EndPoint of(InetSocketAddress addr) {
        return new EndPoint(addr.getHostString(), addr.getPort());
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EndPoint endPoint = (EndPoint) o;
        return port == endPoint.port && Objects.equals(host, endPoint.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }
}
