package com.old.silence.job.client.starter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;


import java.util.Map;
import java.util.Objects;

/**
 * silence job 客户端注册器
 *
 */
public class SilenceJobClientsRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private static final String ENABLED_CONFIG = "silence-job.enabled";
    private static final String GROUP_CONFIG = "silence-job.group";
    protected static final String AOP_ORDER_CONFIG = "silence-job.aop.order";

    protected static final String GROUP_ATTR = "group";
    protected static final String ORDER_ATTR = "order";
    private StandardEnvironment standardEnvironment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
        Map<String, Object> attrs = importingClassMetadata.getAnnotationAttributes(EnableSilenceJob.class.getName());
        Map<String, Object> systemEnvironment = standardEnvironment.getSystemProperties();
        systemEnvironment.put(AOP_ORDER_CONFIG, attrs.get(ORDER_ATTR));

        Object group = attrs.get(GROUP_ATTR);
        // 如果注解内不配置默认取环境变量中的group配置
        if (Objects.nonNull(group) && StringUtils.isNotBlank((CharSequence) group)) {
            systemEnvironment.put(GROUP_CONFIG, group);
        }
    }

    @Override
    public void setEnvironment(@NonNull Environment env) {
        this.standardEnvironment = (StandardEnvironment) env;
        Map<String, Object> systemEnvironment = standardEnvironment.getSystemProperties();
        // 若是用户需要自定义enabled的值，那么以用户的为主
        if (Objects.nonNull(standardEnvironment.getProperty(ENABLED_CONFIG))) {
            return;
        }

        // 添加了 EnableSilenceJob 默认就是开启，无需手动配置
        systemEnvironment.put(ENABLED_CONFIG, Boolean.TRUE);
    }
}
