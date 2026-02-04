package com.old.silence.job.client.core.log;

import com.old.silence.job.client.common.log.report.AbstractLogReport;
import com.old.silence.job.client.common.log.support.SilenceJobLogManager;
import com.old.silence.job.common.dto.JobLogTaskDTO;
import com.old.silence.job.log.dto.LogContentDTO;
import com.old.silence.job.log.enums.LogTypeEnum;
import org.springframework.stereotype.Component;


@Component
public class JobLogReport extends AbstractLogReport<JobLogTaskDTO> {

    @Override
    public boolean supports() {
        return LogTypeEnum.JOB == SilenceJobLogManager.getLogType();
    }

    @Override
    protected JobLogTaskDTO buildLogTaskDTO(LogContentDTO logContentDTO) {
        JobLogMeta context = (JobLogMeta) SilenceJobLogManager.getLogMeta();
        JobLogTaskDTO logTaskDTO = new JobLogTaskDTO();
        logTaskDTO.setJobId(context.getJobId());
        logTaskDTO.setLogType(LogTypeEnum.JOB.name());
        logTaskDTO.setTaskId(context.getTaskId());
        logTaskDTO.setTaskBatchId(context.getTaskBatchId());
        logTaskDTO.setRealTime(logContentDTO.getTimeStamp());
        logTaskDTO.setNamespaceId(context.getNamespaceId());
        logTaskDTO.setGroupName(context.getGroupName());
        logTaskDTO.setFieldList(logContentDTO.getFieldList());
        return logTaskDTO;
    }
}
