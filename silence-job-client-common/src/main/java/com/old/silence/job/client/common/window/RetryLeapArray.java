package com.old.silence.job.client.common.window;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson2.JSON;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.server.dto.RetryTaskDTO;
import com.old.silence.job.common.window.LeapArray;
import com.old.silence.job.common.window.Listener;
import com.old.silence.job.common.window.WindowWrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 通过滑动窗口上报异常数据
 *
 */
public class RetryLeapArray extends LeapArray<ConcurrentLinkedQueue<RetryTaskDTO>> {


    private static final Logger log = LoggerFactory.getLogger(RetryLeapArray.class);
    protected List<Listener<RetryTaskDTO>> listenerList;

    public RetryLeapArray(int sampleCount, int intervalInMs) {
        super(sampleCount, intervalInMs);
    }

    public RetryLeapArray(int sampleCount, int intervalInMs, List<Listener<RetryTaskDTO>> listenerList) {
        super(sampleCount, intervalInMs);
        this.listenerList = listenerList;
    }

    public RetryLeapArray(int sampleCount, int intervalInMs, Listener<RetryTaskDTO> listener) {
        this(sampleCount, intervalInMs, Collections.singletonList(listener));
    }

    @Override
    public ConcurrentLinkedQueue<RetryTaskDTO> newEmptyBucket(long timeMillis) {
        return new ConcurrentLinkedQueue<>();
    }

    @Override
    protected WindowWrap<ConcurrentLinkedQueue<RetryTaskDTO>> resetWindowTo(WindowWrap<ConcurrentLinkedQueue<RetryTaskDTO>> windowWrap, long startTime) {

        ConcurrentLinkedQueue<RetryTaskDTO> deepCopy = new ConcurrentLinkedQueue<>(windowWrap.value());
        try {

            if (CollectionUtils.isNotEmpty(deepCopy)) {
                for (Listener<RetryTaskDTO> listener : listenerList) {
                    listener.handler(new ArrayList<>(deepCopy));
                }
            }
        } catch (Exception e) {
            log.error("滑动窗口监听器处理失败 data:[{}]", JSON.toJSONString(windowWrap.value()), e);
        }

        windowWrap.value().removeAll(deepCopy);
        windowWrap.resetTo(startTime);

        return windowWrap;
    }
}
