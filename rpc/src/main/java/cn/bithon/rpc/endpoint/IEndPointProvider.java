package cn.bithon.rpc.endpoint;

public interface IEndPointProvider {

    class Endpoint {
        private final String host;
        private final int port;

        public Endpoint(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }

    Endpoint getEndpoint();
}
