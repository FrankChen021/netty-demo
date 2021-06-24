package cn.bithon.rpc;

import cn.bithon.rpc.message.ObjectBinarySerializer;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SerializationTest {

    @Test
    public void serialization() throws IOException {
        ObjectBinarySerializer serializer = new ObjectBinarySerializer();

        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            CodedOutputStream os = CodedOutputStream.newInstance(bos);
            serializer.serialize(true, os);
            serializer.serialize(false, os);
            serializer.serialize('a', os);
            serializer.serialize((byte)0xb, os);
            serializer.serialize((short)0xbb, os);
            serializer.serialize(5, os);
            serializer.serialize(50L, os);
            serializer.serialize(5.9f, os);
            serializer.serialize(6.9d, os);
            os.flush();
            bytes = bos.toByteArray();
        }

        CodedInputStream is = CodedInputStream.newInstance(bytes);
        Assert.assertEquals(true, serializer.deserialize(is, boolean.class));
        Assert.assertEquals(false, serializer.deserialize(is, boolean.class));
        Assert.assertEquals((long) 'a', (long) serializer.deserialize(is, char.class));
        Assert.assertEquals(0xb, (long) serializer.deserialize(is, byte.class));
        Assert.assertEquals(0xbb, (long) serializer.deserialize(is, short.class));
        Assert.assertEquals(5, (long) serializer.deserialize(is, int.class));
        Assert.assertEquals((Object) 50L, serializer.deserialize(is, Long.class));
        Assert.assertEquals((Object) 5.9f, serializer.deserialize(is, float.class));
        Assert.assertEquals((Object) 6.9d, serializer.deserialize(is, double.class));
        Assert.assertTrue(is.isAtEnd());
    }

    @Test
    public void serializationProtoBuffer() throws IOException {
        ObjectBinarySerializer serializer = new ObjectBinarySerializer();

        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            CodedOutputStream os = CodedOutputStream.newInstance(bos);
            serializer.serialize(WebRequestMetrics.newBuilder()
                                                  .setCount4Xx(4)
                                                  .setCount5Xx(5)
                                                  .setRequests(9)
                                                  .setUri("/info")
                                                  .build(), os);
            os.flush();
            bytes = bos.toByteArray();
        }

        CodedInputStream is = CodedInputStream.newInstance(bytes);
        WebRequestMetrics metrics = serializer.deserialize(is, WebRequestMetrics.class);
        Assert.assertEquals(4, metrics.getCount4Xx());
        Assert.assertEquals(5, metrics.getCount5Xx());
        Assert.assertEquals(9, metrics.getRequests());
        Assert.assertEquals("/info", metrics.getUri());
        Assert.assertTrue(is.isAtEnd());
    }
}
