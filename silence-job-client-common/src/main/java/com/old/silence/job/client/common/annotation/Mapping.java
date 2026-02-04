package com.old.silence.job.client.common.annotation;


import com.old.silence.job.client.common.rpc.client.RequestMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口定义
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapping {

    /**
     * 请求类型
     */
    RequestMethod method() default RequestMethod.POST;

    /**
     * 请求路径
     */
    String path() default "";

}
