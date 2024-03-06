package com.weikey.multifindhub.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 接口防抖
 *
 * @author wei-key
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestLock {
    /**
     * key的过期时间，默认2秒
     *
     * @return
     */
    int timeout() default 2;

    /**
     * 过期时间单位，默认为秒
     *
     * @return
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 生成key的前缀，不可为空
     *
     * @return
     */
    String prefix() default "";

    /**
     * 生成key的分隔符，默认为【:】
     *
     * @return
     */
    String delimiter() default ":";

    /**
     * key是否包含客户端ip，默认为true
     *
     * @return
     */
    boolean keyContainIP() default true;

}

