package cn.bithon.rpc.example;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExampleServiceImpl implements IExampleService {

    @Override
    public int div(int a, int b) {
        return a / b;
    }

    @Override
    public void block(int timeout) {
        try {
            Thread.sleep(timeout * 1000L);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void send(String msg) {
        System.out.println("Got message:" + msg);
    }

    @Override
    public int[] append(int[] arrays, int value) {
        int[] newArray = Arrays.copyOf(arrays, arrays.length + 1);
        newArray[arrays.length] = value;
        return newArray;
    }

    @Override
    public String[] append(String[] arrays, String value) {
        String[] newArray = Arrays.copyOf(arrays, arrays.length + 1);
        newArray[arrays.length] = value;
        return newArray;
    }

    @Override
    public List<String> delete(List<String> list, int index) {
        list.remove(index);
        return list;
    }

    @Override
    public Map<String, String> merge(Map<String, String> a, Map<String, String> b) {
        if (b != null) {
            if (a != null) {
                b.forEach(a::put);
            } else {
                return b;
            }
        }
        return a;
    }

    @Override
    public String send(WebRequestMetrics metrics) {
        System.out.printf("Receiving metrics: %s\n", metrics);
        return metrics.getUri();
    }
}
