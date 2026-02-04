
package com.old.silence.job.client.common.appender;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.util.Throwables;
import org.slf4j.MDC;
import com.old.silence.job.client.common.log.report.LogReportFactory;
import com.old.silence.job.client.common.log.support.SilenceJobLogManager;
import com.old.silence.job.client.common.rpc.client.NettyChannel;
import com.old.silence.job.log.constant.LogFieldConstants;
import com.old.silence.job.log.dto.LogContentDTO;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;


@Plugin(name = "SilenceLog4j2Appender", category = "Core", elementType = "appender", printObject = true)
public class SilenceLog4j2Appender extends AbstractAppender {

    @Override
    public void append(LogEvent event) {

        // Not job context
        if (Objects.isNull(SilenceJobLogManager.getLogMeta()) || Objects.isNull(MDC.get(LogFieldConstants.MDC_REMOTE))) {
            return;
        }

        MDC.remove(LogFieldConstants.MDC_REMOTE);
        LogContentDTO logContentDTO = new LogContentDTO();
        logContentDTO.addTimeStamp(event.getTimeMillis());
        logContentDTO.addLevelField(event.getLevel().name());
        logContentDTO.addThreadField(event.getThreadName());
        logContentDTO.addLocationField(getLocationField(event));
        logContentDTO.addThrowableField(getThrowableField(event));
        logContentDTO.addMessageField(event.getMessage().getFormattedMessage());
        logContentDTO.addHostField(NettyChannel.getClientHost());
        logContentDTO.addPortField(NettyChannel.getClientPort());

        // slidingWindow syncReportLog
        Optional.ofNullable(LogReportFactory.get()).ifPresent(logReport -> logReport.report(logContentDTO));
    }

    protected SilenceLog4j2Appender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
    }

    /**
     * Create Log4j2Appender
     *
     * @param name       name
     * @param filter     filter
     * @param layout     layout
     * @param ignore     ignore
     * @param timeFormat time format
     * @param timeZone   time zone
     * @return Log4j2Appender
     */
    @PluginFactory
    public static SilenceLog4j2Appender create(
            @PluginAttribute("name") final String name,
            @PluginElement("Filter") final Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute("ignoreExceptions") final String ignore,
            @PluginAttribute("timeFormat") final String timeFormat,
            @PluginAttribute("timeZone") final String timeZone
    ) {
        boolean ignoreExceptions = Booleans.parseBoolean(ignore, false);
        SilenceLog4j2Appender log4j2Appender = new SilenceLog4j2Appender(name, filter, layout, ignoreExceptions);
        return log4j2Appender;
    }

    private String getLocationField(LogEvent event) {
        StackTraceElement source = event.getSource();
        if (source == null && (!event.isIncludeLocation())) {
            event.setIncludeLocation(true);
            source = event.getSource();
            event.setIncludeLocation(false);
        }
        return source == null ? "Unknown(Unknown Source)" : source.toString();
    }

    private String getThrowableField(LogEvent event) {
        String throwable = getThrowableStr(event.getThrown());
        if (throwable != null) {
            return throwable;
        }
        return null;
    }

    private String getThrowableStr(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        int stackDeep = 0;
        for (String s : Throwables.toStringList(throwable)) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(System.getProperty("line.separator"));
            }
            sb.append(s);
            // 最多显示30行
            if (++stackDeep >= 30) {
                break;
            }
        }
        return sb.toString();
    }
}
