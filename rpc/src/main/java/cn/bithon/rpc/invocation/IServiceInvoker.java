package cn.bithon.rpc.invocation;

public interface IServiceInvoker {
    void invoke(ServiceInvocationRunnable runnable);
}
