package cn.bithon.rpc.example;

import cn.bithon.rpc.IService;

public interface INotification extends IService {
    void notify(String message);
}
