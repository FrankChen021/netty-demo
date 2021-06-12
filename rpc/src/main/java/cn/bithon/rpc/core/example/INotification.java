package cn.bithon.rpc.core.example;

import cn.bithon.rpc.core.IService;

public interface INotification extends IService {
    void notify(String message);
}
