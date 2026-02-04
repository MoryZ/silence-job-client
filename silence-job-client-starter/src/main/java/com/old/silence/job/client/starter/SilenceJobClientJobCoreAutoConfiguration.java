package com.old.silence.job.client.starter;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import com.old.silence.job.client.core.annotation.JobExecutor;

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ComponentScan({"com.old.silence.job.client.core"})
@ConditionalOnClass(JobExecutor.class)
@ConditionalOnProperty(prefix = "silence-job", name = "enabled", havingValue = "true")
public class SilenceJobClientJobCoreAutoConfiguration {



}
