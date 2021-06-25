package cn.bithon.rpc;

import cn.bithon.rpc.message.BinarySerializer;
import com.alibaba.fastjson.JSON;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import lombok.Builder;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SerializationTest {

    @Test
    public void serialization() throws IOException {
        BinarySerializer serializer = new BinarySerializer();

        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            CodedOutputStream os = CodedOutputStream.newInstance(bos);
            serializer.serialize(true, os);
            serializer.serialize(false, os);
            serializer.serialize('a', os);
            serializer.serialize((byte) 0xb, os);
            serializer.serialize((short) 0xbb, os);
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
        BinarySerializer serializer = new BinarySerializer();

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

    public static class RequestMetrics extends HashMap<String, WebRequestMetrics> {
    }

    @Test
    public void MapSerialization() throws IOException {
        BinarySerializer serializer = new BinarySerializer();

        RequestMetrics mapObject = new RequestMetrics();
        mapObject.put("/info", WebRequestMetrics.newBuilder()
                                                .setCount4Xx(4)
                                                .setCount5Xx(5)
                                                .setRequests(9)
                                                .setUri("/info")
                                                .build());
        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            CodedOutputStream os = CodedOutputStream.newInstance(bos);
            serializer.serialize(mapObject, os);
            os.flush();
            bytes = bos.toByteArray();
        }

        // customer class
        {
            CodedInputStream is = CodedInputStream.newInstance(bytes);
            RequestMetrics metrics = serializer.deserialize(is, RequestMetrics.class);
            Assert.assertEquals(mapObject, metrics);
            Assert.assertTrue(is.isAtEnd());
        }

        // Map
        {
            CodedInputStream is = CodedInputStream.newInstance(bytes);
            Map<String, WebRequestMetrics> metrics = serializer.deserialize(is, new BinarySerializer.TypeReference<Map<String, WebRequestMetrics>>() {
            });
            Assert.assertEquals(mapObject, metrics);
            Assert.assertTrue(is.isAtEnd());
        }

        // Hash Map
        {
            CodedInputStream is = CodedInputStream.newInstance(bytes);
            Map<String, WebRequestMetrics> metrics = serializer.deserialize(is, new BinarySerializer.TypeReference<HashMap<String, WebRequestMetrics>>() {
            });
            Assert.assertEquals(mapObject, metrics);
            Assert.assertTrue(is.isAtEnd());
        }

        // ConcurrentHashMap
        {
            CodedInputStream is = CodedInputStream.newInstance(bytes);
            Map<String, WebRequestMetrics> metrics = serializer.deserialize(is, new BinarySerializer.TypeReference<ConcurrentHashMap<String, WebRequestMetrics>>() {
            });
            Assert.assertEquals(mapObject, metrics);
            Assert.assertTrue(is.isAtEnd());
        }

        // Hashtable
        {
            CodedInputStream is = CodedInputStream.newInstance(bytes);
            Map<String, WebRequestMetrics> metrics = serializer.deserialize(is, new BinarySerializer.TypeReference<Hashtable<String, WebRequestMetrics>>() {
            });
            Assert.assertEquals(mapObject, metrics);
            Assert.assertTrue(is.isAtEnd());
        }
    }

    @Test
    public void CollectionSerialization() throws IOException {
        BinarySerializer serializer = new BinarySerializer();

        List<WebRequestMetrics> metrics1 = new ArrayList<>();
        metrics1.add(WebRequestMetrics.newBuilder()
                                      .setCount4Xx(4)
                                      .setCount5Xx(5)
                                      .setRequests(9)
                                      .setUri("/info")
                                      .build());
        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            CodedOutputStream os = CodedOutputStream.newInstance(bos);
            serializer.serialize(metrics1, os);
            os.flush();
            bytes = bos.toByteArray();
        }

        // List
        {
            CodedInputStream is = CodedInputStream.newInstance(bytes);
            List<WebRequestMetrics> metrics2 = serializer.deserialize(is,
                                                                      new BinarySerializer.TypeReference<List<WebRequestMetrics>>() {
                                                                      });
            Assert.assertEquals(metrics1, metrics2);
            Assert.assertTrue(is.isAtEnd());
        }

        // ArrayList
        {
            CodedInputStream is = CodedInputStream.newInstance(bytes);
            List<WebRequestMetrics> metrics2 = serializer.deserialize(is,
                                                                      new BinarySerializer.TypeReference<ArrayList<WebRequestMetrics>>() {
                                                                      });
            Assert.assertEquals(metrics1, metrics2);
            Assert.assertTrue(is.isAtEnd());
        }

        // Set
        {
            CodedInputStream is = CodedInputStream.newInstance(bytes);
            Set<WebRequestMetrics> metrics2 = serializer.deserialize(is,
                                                                     new BinarySerializer.TypeReference<Set<WebRequestMetrics>>() {
                                                                     });
            Assert.assertEquals(new HashSet<>(metrics1), metrics2);
            Assert.assertTrue(is.isAtEnd());
        }

        // LinkedList
        {
            CodedInputStream is = CodedInputStream.newInstance(bytes);
            LinkedList<WebRequestMetrics> metrics2 = serializer.deserialize(is,
                                                                            new BinarySerializer.TypeReference<LinkedList<WebRequestMetrics>>() {
                                                                            });
            Assert.assertEquals(metrics1, metrics2);
            Assert.assertTrue(is.isAtEnd());
        }

        // Queue
        {
            CodedInputStream is = CodedInputStream.newInstance(bytes);
            Queue<WebRequestMetrics> metrics2 = serializer.deserialize(is,
                                                                       new BinarySerializer.TypeReference<Queue<WebRequestMetrics>>() {
                                                                       });
            Assert.assertEquals(new LinkedList<>(metrics1), metrics2);
            Assert.assertTrue(is.isAtEnd());
        }
    }

    @Data
    @Builder
    public static class Composite {
        private int a;
        private int b;
    }

    public static class MapObject extends HashMap<String, Composite> {
    }

    @Test
    public void test() {
        String json = "{\"a\":{\"a\":1,\"b\":2}}";
        MapObject mapObject = JSON.parseObject(json, MapObject.class);
    }
}
