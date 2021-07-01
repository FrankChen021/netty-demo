package com.sbss.bithon.component.brpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a method to tell the RPC framework that there's no need to wait for response from RPC server
 * <p>
 * Usually, this annotation should only applied to a method returning 'void'
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Oneway {
}
