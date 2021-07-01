package com.sbss.bithon.component.brpc.example;

import com.sbss.bithon.component.brpc.IService;

public interface INotification extends IService {
    void notify(String message);
}
