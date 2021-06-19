package cn.bithon.rpc.example;

import cn.bithon.rpc.IService;

public interface ICalculator extends IService {
    int div(int a, int b);
}
