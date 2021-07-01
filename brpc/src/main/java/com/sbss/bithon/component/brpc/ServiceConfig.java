package com.sbss.bithon.component.brpc;

import com.sbss.bithon.component.brpc.message.serializer.Serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author frankchen
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ServiceConfig {
    /**
     * service name
     */
    String name() default "";

    boolean isOneway() default false;

    Serializer serializer() default Serializer.BINARY;
}
