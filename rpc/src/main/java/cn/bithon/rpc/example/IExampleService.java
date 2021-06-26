package cn.bithon.rpc.example;

import cn.bithon.rpc.IService;
import cn.bithon.rpc.Oneway;

import java.util.List;
import java.util.Map;

public interface IExampleService extends IService {
    int div(int a, int b);

    /**
     * timeout in seconds
     */
    void block(int timeout);

    /**
     * {@link Oneway} test
     */
    @Oneway
    void send(String msg);

    /**
     * test composite type
     */
    int[] append(int[] arrays, int value);

    String[] append(String[] arrays, String value);

    /**
     * test composite type
     */
    List<String> delete(List<String> list, int index);

    /**
     * test composite type
     */
    Map<String, String> merge(Map<String, String> a, Map<String, String> b);

    String send(WebRequestMetrics metrics);
}
