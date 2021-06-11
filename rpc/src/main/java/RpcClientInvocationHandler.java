import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * interface Calculator {
 * int add(int a, int b);
 * }
 */
public class RpcClientInvocationHandler implements InvocationHandler {

    private final ClientConnection connection;
    private final RpcClientInvocationManager rpcClientInvocationManager;

    public RpcClientInvocationHandler(ClientConnection connection,
                                      RpcClientInvocationManager rpcClientInvocationManager) {
        this.connection = connection;
        this.rpcClientInvocationManager = rpcClientInvocationManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        return rpcClientInvocationManager.sendClientRequest(connection.getChannel(), proxy, method, args);
    }
}
