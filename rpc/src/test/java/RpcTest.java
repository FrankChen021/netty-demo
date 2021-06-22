import cn.bithon.rpc.channel.ClientChannel;
import cn.bithon.rpc.channel.ServerChannel;
import cn.bithon.rpc.example.ICalculator;
import cn.bithon.rpc.exception.ServiceInvocationException;
import cn.bithon.rpc.invocation.IServiceInvoker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class RpcTest {

    ServerChannel serverChannel;

    @Before
    public void setup() {
        serverChannel = new ServerChannel()
            .addService(ICalculator.class, new ICalculator() {

                @Override
                public int div(int a, int b) {
                    return a / b;
                }

                @Override
                public int block(int timeout) {
                    try {
                        Thread.sleep(timeout * 1000L);
                    } catch (InterruptedException ignored) {
                    }
                    return 0;
                }
            }).start(8070).debug(true);
    }

    @After
    public void teardown() {
        System.out.println("TestCase Teardown...");
        serverChannel.close();
    }

    @Test
    public void test() {
        try (ClientChannel ch = new ClientChannel("127.0.0.1", 8070)) {
            ICalculator calculator = ch.getRemoteService(ICalculator.class);
            IServiceInvoker invoker = (IServiceInvoker) calculator;
            invoker.debug(true);
            System.out.println("Start calling");
            Assert.assertEquals(2, calculator.div(6, 3));
            System.out.println("End calling");
        }
    }

    @Test
    public void testInvocationException() {
        try (ClientChannel ch = new ClientChannel("127.0.0.1", 8070)) {
            ICalculator calculator = ch.getRemoteService(ICalculator.class);

            try {
                calculator.div(6, 0);
                Assert.assertTrue(false);
            } catch (ServiceInvocationException e) {
                System.out.println("Exception Occurred when calling RPC:" + e.getMessage());
                Assert.assertTrue(e.getMessage().contains("/ by zero"));
            }
        }
    }

    @Test
    public void testClientSideTimeout() {
        try (ClientChannel ch = new ClientChannel("127.0.0.1", 8070)) {
            ICalculator calculator = ch.getRemoteService(ICalculator.class);

            calculator.block(2);

            try {
                calculator.block(6);
                Assert.assertTrue(false);
            } catch (ServiceInvocationException e) {
                Assert.assertTrue(true);
            }

            calculator.toInvoker().setTimeout(2000);
            try {
                calculator.block(3);
                Assert.assertTrue(false);
            } catch (ServiceInvocationException e) {
                Assert.assertTrue(true);
            }
        }
    }

    @Test
    public void testConcurrent() {
        try (ClientChannel ch = new ClientChannel("127.0.0.1", 8070)) {
            ICalculator calculator = ch.getRemoteService(ICalculator.class);

            AtomicInteger v = new AtomicInteger();
            AtomicInteger i = new AtomicInteger();
            ThreadLocalRandom.current().ints(1000, 5, 1000).parallel().forEach(divisor -> {
                try {
                    int idx = i.incrementAndGet();
                    int val = calculator.div(divisor, 1);
                    if (val != divisor) {
                        v.incrementAndGet();
                    }
                    System.out.printf("%s:%d, ret=%s\n", Thread.currentThread().getName(), idx, val == divisor);
                } catch (ServiceInvocationException e) {
                    System.out.println(e.getMessage());
                    v.incrementAndGet();
                }
            });

            Assert.assertEquals(0, v.get());
        }
    }
}

/**
 * 1. server端已经有work group，是否还需要自定义的thread pool执行操作？
 * 2. 自定义的encoder/decoder
 */
