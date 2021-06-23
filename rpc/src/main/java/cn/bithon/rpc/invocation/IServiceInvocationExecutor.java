package cn.bithon.rpc.invocation;

public interface IServiceInvocationExecutor {
    void invoke(ServiceInvocationRunnable runnable);
}
