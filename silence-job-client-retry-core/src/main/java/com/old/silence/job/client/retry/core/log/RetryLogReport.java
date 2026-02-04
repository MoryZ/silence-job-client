package com.old.silence.job.client.retry.core.log;

import org.springframework.stereotype.Component;
import com.old.silence.job.client.common.log.report.AbstractLogReport;
import com.old.silence.job.client.common.log.support.SilenceJobLogManager;
import com.old.silence.job.common.server.dto.RetryLogTaskDTO;
import com.old.silence.job.log.dto.LogContentDTO;
import com.old.silence.job.log.enums.LogTypeEnum;


@Component
public class RetryLogReport extends AbstractLogReport<RetryLogTaskDTO> {
    @Override
    protected RetryLogTaskDTO buildLogTaskDTO(LogContentDTO logContentDTO) {
        RetryLogMeta context = (RetryLogMeta) SilenceJobLogManager.getLogMeta();
        RetryLogTaskDTO logTaskDTO = new RetryLogTaskDTO();
        logTaskDTO.setLogType(LogTypeEnum.RETRY.name());
        logTaskDTO.setRetryId(context.getRetryId());
        logTaskDTO.setRetryTaskId(context.getRetryTaskId());
        logTaskDTO.setRealTime(logContentDTO.getTimeStamp());
        logTaskDTO.setNamespaceId(context.getNamespaceId());
        logTaskDTO.setGroupName(context.getGroupName());
        logTaskDTO.setFieldList(logContentDTO.getFieldList());
        return logTaskDTO;
    }

    @Override
    public boolean supports() {
        return LogTypeEnum.RETRY == SilenceJobLogManager.getLogType();
    }
}
