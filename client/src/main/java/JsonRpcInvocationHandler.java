import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Data;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * interface Calculator {
 * int add(int a, int b);
 * }
 */
public class JsonRpcInvocationHandler implements InvocationHandler {

    private final static AtomicLong transactionId = new AtomicLong();

    private final ClientConnection connection;

    private final ObjectMapper om = new JsonMapper();

    public JsonRpcInvocationHandler(ClientConnection connection) {
        this.connection = connection;
    }

    @Data
    static class Rpc {
        private String serviceName;
        private String methodName;
        private Long transactionId;
        private Object[] args;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Rpc rpc = new Rpc();
        rpc.serviceName = method.getDeclaringClass().getSimpleName();
        rpc.methodName = method.getName();
        rpc.transactionId = transactionId.incrementAndGet();
        rpc.args = args;
        connection.getChannel().writeAndFlush(om.writeValueAsString(rpc));
        return null;
    }
}
