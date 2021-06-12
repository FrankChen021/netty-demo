package cn.bithon.rpc.core.example;

import cn.bithon.rpc.core.IService;

public interface ICalculator extends IService {
    int div(int a, int b);
}
