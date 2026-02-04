package com.old.silence.job.client.core.annotation;

import com.old.silence.job.common.constant.SystemConstants;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MapExecutor {

    /**
     * 任务名称
     *
     */
    String taskName() default SystemConstants.ROOT_MAP;

}
