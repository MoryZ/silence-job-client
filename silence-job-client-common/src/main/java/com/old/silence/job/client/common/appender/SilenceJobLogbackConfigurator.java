package com.old.silence.job.client.common.appender;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;

/**
 * SilenceJob Logback Configurator - 通过 SPI 自动注册 SilenceLogbackAppender
 * <p>
 * 当 Logback 初始化时，Spring Boot 会自动发现并调用此 Configurator，
 * 将 SilenceLogbackAppender 注册到 root logger，实现零配置。
 */
public class SilenceJobLogbackConfigurator implements Configurator {

    private Context context;

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void addStatus(Status status) {
        if (context != null) {
            context.getStatusManager().add(status);
        }
    }

    @Override
    public void addInfo(String msg) {
        addStatus(new InfoStatus(msg, this));
    }

    @Override
    public void addInfo(String msg, Throwable ex) {
        addStatus(new InfoStatus(msg, this, ex));
    }

    @Override
    public void addWarn(String msg) {
        addStatus(new InfoStatus(msg, this));
    }

    @Override
    public void addWarn(String msg, Throwable ex) {
        addStatus(new InfoStatus(msg, this, ex));
    }

    @Override
    public void addError(String msg) {
        addStatus(new ErrorStatus(msg, this));
    }

    @Override
    public void addError(String msg, Throwable ex) {
        addStatus(new ErrorStatus(msg, this, ex));
    }

    @Override
    public ExecutionStatus configure(LoggerContext loggerContext) {
        System.err.println("[SilenceJobLogbackConfigurator] configure() called!");
        setContext(loggerContext);
        
        // 检查是否已经注册过
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        System.err.println("[SilenceJobLogbackConfigurator] RootLogger effective level: " + rootLogger.getEffectiveLevel());
        
        if (rootLogger.getAppender("SILENCE_JOB_APPENDER") != null) {
            addInfo("SilenceLogbackAppender already registered, skipping.");
            return ExecutionStatus.NEUTRAL;
        }

        try {
            addInfo("Setting up SilenceLogbackAppender...");
            System.err.println("[SilenceJobLogbackConfigurator] Creating SilenceLogbackAppender...");
            
            SilenceLogbackAppender appender = new SilenceLogbackAppender();
            appender.setContext(loggerContext);
            appender.setName("SILENCE_JOB_APPENDER");
            appender.start();
            
            System.err.println("[SilenceJobLogbackConfigurator] Appender started, adding to root logger...");
            rootLogger.addAppender(appender);
            
            System.err.println("[SilenceJobLogbackConfigurator] Appender registered successfully!");
            addInfo("SilenceLogbackAppender registered successfully.");
        } catch (Exception e) {
            addError("Failed to register SilenceLogbackAppender", e);
            e.printStackTrace();
        }
        
        return ExecutionStatus.NEUTRAL;
    }
}
