package com.old.silence.job.client.common.appender;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.slf4j.MDC;

import java.util.Objects;
import java.util.Optional;

import com.old.silence.job.client.common.log.report.LogReportFactory;
import com.old.silence.job.client.common.log.support.SilenceJobLogManager;
import com.old.silence.job.client.common.rpc.client.NettyChannel;
import com.old.silence.job.log.constant.LogFieldConstants;
import com.old.silence.job.log.dto.LogContentDTO;

/**
 * SilenceJob Logback Appender - 将 Job 执行期间的日志上报到服务器
 */
public class SilenceLogbackAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    @Override
    public void start() {
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        System.err.println("[SilenceLogbackAppender] append called: " + event.getFormattedMessage());
        
        if (Objects.isNull(SilenceJobLogManager.getLogMeta())) {
            System.err.println("[SilenceLogbackAppender] LogMeta is null, skipping.");
            return;
        }
        if (Objects.isNull(MDC.get(LogFieldConstants.MDC_REMOTE))) {
            System.err.println("[SilenceLogbackAppender] MDC_REMOTE is null, skipping.");
            return;
        }

        System.err.println("[SilenceLogbackAppender] Processing log: " + event.getFormattedMessage());
        MDC.remove(LogFieldConstants.MDC_REMOTE);
        
        LogContentDTO logContentDTO = new LogContentDTO();

        logContentDTO.addTimeStamp(event.getTimeStamp());
        logContentDTO.addLevelField(event.getLevel().levelStr);
        logContentDTO.addThreadField(event.getThreadName());
        logContentDTO.addMessageField(event.getFormattedMessage());
        logContentDTO.addLocationField(getLocationField(event));
        logContentDTO.addThrowableField(getThrowableField(event));
        logContentDTO.addHostField(NettyChannel.getClientHost());
        logContentDTO.addPortField(NettyChannel.getClientPort());

        System.err.println("[SilenceLogbackAppender] LogReportFactory.get() = " + LogReportFactory.get());
        String message = event.getFormattedMessage();
        Optional.ofNullable(LogReportFactory.get()).ifPresent(logReport -> {
            System.err.println("[SilenceLogbackAppender] Reporting log: " + message);
            logReport.report(logContentDTO);
        });
    }

    private String getLocationField(ILoggingEvent event) {
        StackTraceElement[] caller = event.getCallerData();
        if (caller != null && caller.length > 0) {
            return caller[0].toString();
        }
        return null;
    }

    private String getThrowableField(ILoggingEvent event) {
        IThrowableProxy iThrowableProxy = event.getThrowableProxy();
        if (iThrowableProxy == null) {
            return null;
        }
        return getExceptionInfo(iThrowableProxy) +
                formatThrowable(iThrowableProxy.getStackTraceElementProxyArray());
    }

    private String formatThrowable(StackTraceElementProxy[] stackTraceElementProxyArray) {
        if (stackTraceElementProxyArray == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int stackDeep = 0;
        for (StackTraceElementProxy step : stackTraceElementProxyArray) {
            builder.append(CoreConstants.LINE_SEPARATOR);
            String string = step.toString();
            builder.append(CoreConstants.TAB).append(string);
            ThrowableProxyUtil.subjoinPackagingData(builder, step);
            if (++stackDeep >= 30) {
                break;
            }
        }
        return builder.toString();
    }

    private String getExceptionInfo(IThrowableProxy iThrowableProxy) {
        String s = iThrowableProxy.getClassName();
        String message = iThrowableProxy.getMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
