package com.old.silence.job.client.core.dto;




import java.util.List;

/**
 * Task执行结果
 *
 */

public class MergeReduceArgs extends JobArgs {

    private List<?> reduces;

    public List<?> getReduces() {
        return reduces;
    }

    public void setReduces(List<?> reduces) {
        this.reduces = reduces;
    }
}
