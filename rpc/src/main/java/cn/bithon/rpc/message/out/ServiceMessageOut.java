package cn.bithon.rpc.message.out;

import cn.bithon.rpc.message.ServiceMessage;
import com.google.protobuf.CodedOutputStream;

import java.io.IOException;

public abstract class ServiceMessageOut extends ServiceMessage {

    public abstract void encode(CodedOutputStream out) throws IOException;


}
