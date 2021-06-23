package cn.bithon.rpc.example;

import cn.bithon.rpc.IService;
import cn.bithon.rpc.Oneway;

public interface IExampleService extends IService {
    int div(int a, int b);

    /**
     * timeout in seconds
     */
    int block(int timeout);

    /**
     *
     */
    @Oneway
    void send(String msg);
}
