import cn.bithon.rpc.channel.ClientChannel;
import cn.bithon.rpc.channel.ServerChannel;
import cn.bithon.rpc.example.ICalculator;
import cn.bithon.rpc.exception.ServiceInvocationException;
import cn.bithon.rpc.invocation.IServiceInvoker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
            }).start(8070);
    }

    @After
    public void teardown() {
        serverChannel.shutdown();
    }

    @Test
    public void test() {
        ICalculator calculator = new ClientChannel("127.0.0.1", 8070).getRemoteService(ICalculator.class);
        IServiceInvoker invoker = (IServiceInvoker)calculator;
        invoker.debug(true);
        Assert.assertEquals(2, calculator.div(6, 3));
    }

    @Test
    public void testInvocationException() {
        ICalculator calculator = new ClientChannel("127.0.0.1", 8070).getRemoteService(ICalculator.class);
        try {
            calculator.div(6, 0);
            Assert.assertTrue(false);
        } catch (ServiceInvocationException e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(e.getMessage().contains("/ by zero"));
        }
    }

    @Test
    public void testClientSideTimeout() {
        ICalculator calculator = new ClientChannel("127.0.0.1", 8070).getRemoteService(ICalculator.class);
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
