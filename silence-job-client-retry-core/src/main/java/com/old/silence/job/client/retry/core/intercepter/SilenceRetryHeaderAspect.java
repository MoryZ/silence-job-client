package com.old.silence.job.client.retry.core.intercepter;

import jakarta.servlet.http.HttpServletResponse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.model.SilenceJobHeaders;
import com.old.silence.job.log.SilenceJobLog;

import java.util.Objects;

/**
 * 服务间调用传递请求头和响应头
 *
 */
@Aspect
@Component
public class SilenceRetryHeaderAspect {

    public void before() {
        if (skip()) {
            return;
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(attributes)) {
            return;
        }

        String xRetry = attributes.getRequest().getHeader(SystemConstants.SILENCE_JOB_HEAD_KEY);
        if (Objects.nonNull(xRetry)) {
            // 标记进入方法的时间
            RetrySiteSnapshot.setEntryMethodTime(System.currentTimeMillis());

            SilenceJobLog.LOCAL.info("silence retry request header :[{}]", xRetry);
            RetrySiteSnapshot.setRetryHeader(JSON.parseObject(xRetry, SilenceJobHeaders.class));
        }
    }

    @Around(value = "@within(org.springframework.web.bind.annotation.RestController)")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        before();

        Throwable throwable = null;
        Object result = null;
        try {
            result = point.proceed();
        } catch (Throwable t) {
            throwable = t;
        } finally {
            afterReturning();
        }

        if (throwable != null) {
            throw throwable;
        } else {
            return result;
        }
    }

    public void afterReturning() {

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(attributes)) {
            return;
        }

        HttpServletResponse response = attributes.getResponse();
        if (Objects.nonNull(response)) {
            response.addHeader(SystemConstants.SILENCE_JOB_STATUS_CODE_KEY, RetrySiteSnapshot.getRetryStatusCode());
        }

        if (skip()) {
            return;
        }

        // 服务端重试的在com.old.silence.silence.job.client.client.SilenceRetryEndPoint.dispatch 中进行清除threadLocal
        if (Objects.nonNull(RetrySiteSnapshot.getStage()) && RetrySiteSnapshot.EnumStage.REMOTE.getStage() == RetrySiteSnapshot.getStage()) {
            return;
        }

        // 这里清除是为了,非服务端直接触发的节点，需要清除 threadLocal里面的标记
        RetrySiteSnapshot.removeRetryHeader();
        RetrySiteSnapshot.removeRetryStatusCode();
        RetrySiteSnapshot.removeEntryMethodTime();

    }

    /**
     * 本地重试不执行afterReturning和before方法，避免header传递失效
     *
     */
    private boolean skip() {

        Integer stage = RetrySiteSnapshot.getStage();
        if (Objects.nonNull(stage) && RetrySiteSnapshot.EnumStage.LOCAL.getStage() == RetrySiteSnapshot.getStage()) {
            return true;
        }

        return false;
    }
}
