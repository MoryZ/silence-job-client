package com.old.silence.job.client.retry.core.exception;

import com.old.silence.job.common.exception.BaseSilenceJobException;


public class SilenceRetryClientException extends BaseSilenceJobException {

    public SilenceRetryClientException(String message) {
        super(message);
    }

    public SilenceRetryClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public SilenceRetryClientException(Throwable cause) {
        super(cause);
    }

    public SilenceRetryClientException(String message, Object... arguments) {
        super(message, arguments);
    }

    public SilenceRetryClientException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public SilenceRetryClientException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public SilenceRetryClientException(String message, Object argument) {
        super(message, argument);
    }
}
