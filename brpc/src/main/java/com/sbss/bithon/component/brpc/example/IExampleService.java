package com.sbss.bithon.component.brpc.example;

import com.sbss.bithon.component.brpc.IService;
import com.sbss.bithon.component.brpc.Oneway;

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

    /**
     * test multiple protobuf messages
     */
    String send(WebRequestMetrics metrics1, WebRequestMetrics metrics2);
    String send(String uri, WebRequestMetrics metrics);
    String send(WebRequestMetrics metrics, String uri);
}
