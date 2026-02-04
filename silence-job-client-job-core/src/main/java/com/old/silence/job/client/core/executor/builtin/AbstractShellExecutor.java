package com.old.silence.job.client.core.executor.builtin;


import java.math.BigInteger;

import static com.old.silence.job.client.core.executor.builtin.AbstractScriptExecutor.SH_SHELL;

public abstract class AbstractShellExecutor extends AbstractScriptExecutor {

    @Override
    protected String getScriptName(BigInteger jobId) {
        return String.format("shell_%d.sh", jobId);
    }

    @Override
    protected String getRunCommand() {
        return SH_SHELL;
    }

    @Override
    protected ProcessBuilder getScriptProcessBuilder(String scriptPath) {
        return new ProcessBuilder("sh", scriptPath);
    }
}
