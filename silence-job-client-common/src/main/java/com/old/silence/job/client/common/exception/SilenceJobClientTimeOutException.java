package com.old.silence.job.client.common.exception;

import com.old.silence.job.common.exception.BaseSilenceJobException;


public class SilenceJobClientTimeOutException extends BaseSilenceJobException {

    public SilenceJobClientTimeOutException(String message) {
        super(message);
    }

    public SilenceJobClientTimeOutException(String message, Throwable cause) {
        super(message, cause);
    }

    public SilenceJobClientTimeOutException(Throwable cause) {
        super(cause);
    }

    public SilenceJobClientTimeOutException(String message, Object... arguments) {
        super(message, arguments);
    }

    public SilenceJobClientTimeOutException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public SilenceJobClientTimeOutException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public SilenceJobClientTimeOutException(String message, Object argument) {
        super(message, argument);
    }
}
