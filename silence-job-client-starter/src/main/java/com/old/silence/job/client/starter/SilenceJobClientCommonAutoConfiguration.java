package com.old.silence.job.client.starter;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * SilenceJob 客户端通用自动配置
 * 扫描 common 包中的公共组件
 * 仅当 silence-job.enabled=true 时激活
 *
 * @author mory
 */
@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ComponentScan({"com.old.silence.job.client.common"})
@ConditionalOnProperty(prefix = "silence-job", name = "enabled", havingValue = "true")
public class SilenceJobClientCommonAutoConfiguration {

}
