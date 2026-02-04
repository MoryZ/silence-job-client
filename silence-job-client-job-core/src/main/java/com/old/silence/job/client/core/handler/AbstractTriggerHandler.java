package com.old.silence.job.client.core.handler;

import cn.hutool.core.util.StrUtil;


import com.alibaba.fastjson2.JSON;
import com.old.silence.job.client.core.dto.JobTriggerDTO;
import com.old.silence.job.common.util.JsonUtils;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


public abstract class AbstractTriggerHandler<H, R> extends AbstractRequestHandler<R> {
    private final JobTriggerDTO reqDTO;
    private H r;

    public AbstractTriggerHandler(BigInteger jobId) {
        this.reqDTO = new JobTriggerDTO();
       // 设置调度id
        reqDTO.setJobId(jobId);
    }


    /**
     * 添加参数，可支持多次添加
     * 静态分片不可使用该方法
     *
     * @param argsKey   参数名
     * @param argsValue 参数值
     */
    protected H addArgsStr(String argsKey, Object argsValue) {
        Map<String, Object> map = new HashMap<>();
        if (StrUtil.isNotBlank(reqDTO.getTmpArgsStr())) {
            map = JsonUtils.parseHashMap(reqDTO.getTmpArgsStr());
        }
        map.put(argsKey, argsValue);
        reqDTO.setTmpArgsStr(JSON.toJSONString(map));
        return r;
    }

    /**
     * 添加静态分片相关参数
     *
     * @param shardingValue 分片参数
     * @return r
     */
    protected H addShardingArgs(String... shardingValue) {
        reqDTO.setTmpArgsStr(JSON.toJSONString(shardingValue));
        return r;
    }

    protected abstract void afterExecute(Boolean aBoolean);

    public JobTriggerDTO getReqDTO() {
        return reqDTO;
    }

    public H getR() {
        return r;
    }

    public void setR(H r) {
        this.r = r;
    }
}
