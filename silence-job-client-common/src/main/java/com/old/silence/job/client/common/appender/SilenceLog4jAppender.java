
package com.old.silence.job.client.common.appender;


import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.slf4j.MDC;
import com.old.silence.job.client.common.log.report.LogReportFactory;
import com.old.silence.job.client.common.log.support.SilenceJobLogManager;
import com.old.silence.job.client.common.rpc.client.NettyChannel;
import com.old.silence.job.log.constant.LogFieldConstants;
import com.old.silence.job.log.dto.LogContentDTO;

import java.util.Objects;
import java.util.Optional;


public class SilenceLog4jAppender extends AppenderSkeleton {

    @Override
    public void activateOptions() {
        super.activateOptions();
    }

    @Override
    protected void append(LoggingEvent event) {

        // Not job context
        if (Objects.isNull(SilenceJobLogManager.getLogMeta()) || Objects.isNull(MDC.get(LogFieldConstants.MDC_REMOTE))) {
            return;
        }

        MDC.remove(LogFieldConstants.MDC_REMOTE);
        LogContentDTO logContentDTO = new LogContentDTO();
        logContentDTO.addTimeStamp(event.getTimeStamp());
        logContentDTO.addLevelField(event.getLevel().toString());
        logContentDTO.addThreadField(event.getThreadName());
        logContentDTO.addMessageField(event.getMessage().toString());
        logContentDTO.addLocationField(event.getLocationInformation().fullInfo);
        logContentDTO.addThrowableField(getThrowableField(event));
        logContentDTO.addHostField(NettyChannel.getClientHost());
        logContentDTO.addPortField(NettyChannel.getClientPort());

        // slidingWindow syncReportLog
        Optional.ofNullable(LogReportFactory.get()).ifPresent(logReport -> logReport.report(logContentDTO));
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    private String getThrowableField(LoggingEvent event) {
        String throwable = getThrowableStr(event);
        if (throwable != null) {
            return throwable;
        }
        return null;
    }

    private String getThrowableStr(LoggingEvent event) {
        ThrowableInformation throwable = event.getThrowableInformation();
        if (throwable == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        int stackDeep = 0;
        for (String s : throwable.getThrowableStrRep()) {
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
