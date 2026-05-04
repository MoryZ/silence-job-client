
package com.old.silence.job.client.core.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * job执行者
 * <p>
 * 类级别使用时，执行器必须继承AbstractJobExecutor，
 * 并通过重写doJobExecute(JobArgs)实现业务逻辑。
 * 方法级别使用时，直接将该方法注册为执行入口。
 *
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface JobExecutor {

    /**
     * 执行器名称
     */
    String name();

    /**
     * 执行器方法
     * <p>
     * 仅在类级别注解模式下生效。建议优先通过重写doJobExecute(JobArgs)实现，
     * 避免隐式方法名约定。
     */
    String method() default "execute";
}
