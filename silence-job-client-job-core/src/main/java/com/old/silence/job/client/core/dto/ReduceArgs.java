package com.old.silence.job.client.core.dto;




import java.util.List;

/**
 * Task执行结果
 *
 */

public class ReduceArgs extends JobArgs {

    private List<?> mapResult;

    public List<?> getMapResult() {
        return mapResult;
    }

    public void setMapResult(List<?> mapResult) {
        this.mapResult = mapResult;
    }
}
