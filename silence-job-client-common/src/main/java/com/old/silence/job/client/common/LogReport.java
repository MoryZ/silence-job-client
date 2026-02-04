package com.old.silence.job.client.common;


import com.old.silence.job.log.dto.LogContentDTO;

public interface LogReport {

    boolean supports();

    void report(LogContentDTO logContentDTO);
}
