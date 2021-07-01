package com.sbss.bithon.component.brpc.message.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * serializer for service arguments and returning object
 */
public enum Serializer {
    BINARY {
        private final ProtocolBufferSerializer serializer = new ProtocolBufferSerializer();

        @Override
        public int getType() {
            return 0x525;
        }

        @Override
        public void serialize(CodedOutputStream os, Object obj) throws IOException {
            if (obj != null) {
                os.writeRawByte(1);
                serializer.serialize(obj, os);
            } else {
                os.writeRawByte(0);
            }
        }

        @Override
        public Object deserialize(CodedInputStream is, Type type) throws IOException {
            if (is.readRawByte() == 1) {
                return serializer.deserialize(is, type);
            } else {
                return null;
            }
        }
    },
    JSON {
        private final ObjectMapper om = new ObjectMapper();

        @Override
        public int getType() {
            return 0x629;
        }

        @Override
        public void serialize(CodedOutputStream os, Object obj) throws IOException {
            if (obj == null) {
                os.writeByteArrayNoTag(new byte[0]);
                return;
            }
            os.writeByteArrayNoTag(om.writeValueAsBytes(obj));
        }

        @Override
        public Object deserialize(CodedInputStream is, Type type) throws IOException {
            byte[] bytes = is.readByteArray();
            if (bytes.length != 0) {
                return om.readValue(bytes, om.constructType(type));
            }
            return null;
        }
    };

    /**
     * type of serializer
     * Encoded in the message for deserialization
     */
    public abstract int getType();

    public abstract void serialize(CodedOutputStream os, Object obj) throws IOException;

    public abstract Object deserialize(CodedInputStream is, Type type) throws IOException;

    public static Serializer getSerializer(int type) {
        if (type == BINARY.getType()) {
            return BINARY;
        }
        if (type == JSON.getType()) {
            return JSON;
        }
        throw new IllegalArgumentException("Unknown serializer: " + type);
    }
}
