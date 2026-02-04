package com.old.silence.job.client.common.exception;

import com.old.silence.job.common.exception.BaseSilenceJobException;


public class SilenceJobRemoteException extends BaseSilenceJobException {

    public SilenceJobRemoteException(String message) {
        super(message);
    }

    public SilenceJobRemoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public SilenceJobRemoteException(Throwable cause) {
        super(cause);
    }

    public SilenceJobRemoteException(String message, Object... arguments) {
        super(message, arguments);
    }

    public SilenceJobRemoteException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public SilenceJobRemoteException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public SilenceJobRemoteException(String message, Object argument) {
        super(message, argument);
    }
}
