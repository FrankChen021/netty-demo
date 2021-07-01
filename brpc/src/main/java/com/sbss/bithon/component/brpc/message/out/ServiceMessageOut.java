package com.sbss.bithon.component.brpc.message.out;

import com.google.protobuf.CodedOutputStream;
import com.sbss.bithon.component.brpc.message.ServiceMessage;

import java.io.IOException;

public abstract class ServiceMessageOut extends ServiceMessage {

    public abstract void encode(CodedOutputStream out) throws IOException;
}
