package com.old.silence.job.client.common.exception;

import com.old.silence.job.common.exception.BaseSilenceJobException;


public class SilenceJobClientException extends BaseSilenceJobException {

    public SilenceJobClientException(String message) {
        super(message);
    }

    public SilenceJobClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public SilenceJobClientException(Throwable cause) {
        super(cause);
    }

    public SilenceJobClientException(String message, Object... arguments) {
        super(message, arguments);
    }

    public SilenceJobClientException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public SilenceJobClientException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public SilenceJobClientException(String message, Object argument) {
        super(message, argument);
    }
}
