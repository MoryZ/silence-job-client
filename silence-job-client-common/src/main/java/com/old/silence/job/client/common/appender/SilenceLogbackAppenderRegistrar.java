package com.old.silence.job.client.common.appender;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SilenceJob Logback Appender 注册器
 * <p>
 * 通过 Spring PostConstruct 在容器启动后注册 Appender，
 * 确保所有 Spring Bean 都已初始化完成。
 */
@Component
public class SilenceLogbackAppenderRegistrar {

    @PostConstruct
    public void registerAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        
        // 检查是否已经注册过
        if (rootLogger.getAppender("SILENCE_JOB_APPENDER") != null) {
            return;
        }

        try {
            SilenceLogbackAppender appender = new SilenceLogbackAppender();
            appender.setContext(loggerContext);
            appender.setName("SILENCE_JOB_APPENDER");
            appender.start();
            
            rootLogger.addAppender(appender);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
