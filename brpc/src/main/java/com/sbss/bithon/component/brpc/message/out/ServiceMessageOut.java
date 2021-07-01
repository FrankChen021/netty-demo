package com.sbss.bithon.component.brpc.message.out;

import com.google.protobuf.CodedOutputStream;
import com.sbss.bithon.component.brpc.message.ServiceMessage;
import com.sbss.bithon.component.brpc.message.serializer.Serializer;

import java.io.IOException;

public abstract class ServiceMessageOut extends ServiceMessage {

    private Serializer serializer = Serializer.BINARY;

    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public abstract void encode(CodedOutputStream out) throws IOException;
}
