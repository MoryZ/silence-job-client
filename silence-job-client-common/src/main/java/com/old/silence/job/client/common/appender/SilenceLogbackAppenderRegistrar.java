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
        
        System.err.println("[SilenceLogbackAppenderRegistrar] PostConstruct called!");
        System.err.println("[SilenceLogbackAppenderRegistrar] RootLogger effective level: " + rootLogger.getEffectiveLevel());
        
        // 检查是否已经注册过
        if (rootLogger.getAppender("SILENCE_JOB_APPENDER") != null) {
            System.err.println("[SilenceLogbackAppenderRegistrar] Already registered, skipping.");
            return;
        }

        try {
            SilenceLogbackAppender appender = new SilenceLogbackAppender();
            appender.setContext(loggerContext);
            appender.setName("SILENCE_JOB_APPENDER");
            appender.start();
            
            rootLogger.addAppender(appender);
            
            System.err.println("[SilenceLogbackAppenderRegistrar] Appender registered successfully!");
        } catch (Exception e) {
            System.err.println("[SilenceLogbackAppenderRegistrar] Failed to register appender: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
