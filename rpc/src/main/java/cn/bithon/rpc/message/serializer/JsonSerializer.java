package cn.bithon.rpc.message.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import java.io.IOException;
import java.lang.reflect.Type;

public class JsonSerializer implements ISerializer {
    public static JsonSerializer INSTANCE = new JsonSerializer();

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
}
