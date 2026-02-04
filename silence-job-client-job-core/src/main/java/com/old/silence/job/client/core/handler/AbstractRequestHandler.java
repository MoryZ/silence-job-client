package com.old.silence.job.client.core.handler;

import cn.hutool.core.lang.Pair;
import com.old.silence.job.client.common.exception.SilenceJobClientException;


public abstract class AbstractRequestHandler<R> implements RequestHandler<R> {

    /**
     * 具体调用
     */
    @Override
    public R execute() {
        Pair<Boolean, String> checked = checkRequest();
        if (checked.getKey()) {
            beforeExecute();
            R r = doExecute();
            afterExecute(r);
            return r;
        } else {
            throw new SilenceJobClientException("silence job openapi check error. [{}]", checked.getValue());
        }
    }

    protected abstract void afterExecute(R r);

    protected abstract void beforeExecute();

    protected abstract R doExecute();

    protected abstract Pair<Boolean, String> checkRequest();
}
