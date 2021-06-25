package cn.bithon.rpc.invocation;

import cn.bithon.rpc.message.out.ServiceResponseMessageOut;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ThreadPoolInvocationExecutor implements IServiceInvocationExecutor {

    private final Executor executor;

    public ThreadPoolInvocationExecutor(int threadPoolSize) {
        executor = new ThreadPoolExecutor(threadPoolSize,
                                          threadPoolSize,
                                          0L,
                                          TimeUnit.MILLISECONDS,
                                          new LinkedBlockingQueue<>(4096),
                                          new RejectHandler());
    }

    @Override
    public void invoke(ServiceInvocationRunnable runnable) {
        executor.execute(runnable);
    }

    static class RejectHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            ServiceInvocationRunnable invoker = (ServiceInvocationRunnable) r;

            invoker.getChannel()
                   .writeAndFlush(ServiceResponseMessageOut.builder()
                                                           .serverResponseAt(System.currentTimeMillis())
                                                           .transactionId(invoker.getServiceRequest()
                                                                                 .getTransactionId())
                                                           .exception(
                                                               "Server has no enough resources to process the request.")
                                                           .build());
        }
    }
}
