
package com.old.silence.job.client.starter;

import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在启动类上添加SilenceJobRetry注解开启silence job功能
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SilenceJobClientsRegistrar.class)
public @interface EnableSilenceJob {

    /**
     * 请在服务端提前配置好组,并设置在这里
     * group的配置支持注解和配置文件两种形式
     * 配置顺序为注解 > yml
     * 即: 如果注解内不配置默认取环境变量中的group配置
     * 比如:
     * <p>
     * silence-job.group = silence_job_demo_group
     * </p>
     *
     * @return group
     */
    String group() default "";

    /**
     * 控制多个Aop的执行顺序,
     * 需要注意的是这里顺序要比事务的Aop要提前
     * <p>
     * see {@link  EnableTransactionManagement#order()}
     * 默认值: Ordered.HIGHEST_PRECEDENCE
     */
    int order() default Ordered.HIGHEST_PRECEDENCE;


}
