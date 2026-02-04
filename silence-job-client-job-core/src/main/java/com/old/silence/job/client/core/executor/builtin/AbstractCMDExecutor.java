package com.old.silence.job.client.core.executor.builtin;

import java.math.BigInteger;
import java.nio.charset.Charset;


public abstract class AbstractCMDExecutor extends AbstractScriptExecutor {

    @Override
    protected String getScriptName(BigInteger jobId) {
        return String.format("cmd_%d.bat", jobId);
    }

    @Override
    protected String getRunCommand() {
        return "cmd.exe";
    }

    @Override
    protected Charset getCharset() {
        return Charset.defaultCharset();
    }

    @Override
    protected ProcessBuilder getScriptProcessBuilder(String scriptPath) {
        return new ProcessBuilder(getRunCommand(), "/c", scriptPath);
    }
}
