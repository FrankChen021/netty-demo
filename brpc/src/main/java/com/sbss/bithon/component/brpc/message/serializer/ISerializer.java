package com.sbss.bithon.component.brpc.message.serializer;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * serializer for service arguments and returning object
 */
public interface ISerializer {
    /**
     * type of serializer
     * Encoded in the message for deserialization
     */
    int getType();

    void serialize(CodedOutputStream os, Object obj) throws IOException;

    Object deserialize(CodedInputStream is, Type type) throws IOException;
}
