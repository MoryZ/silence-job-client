package com.old.silence.job.client.core.executor.builtin;

import java.math.BigInteger;
import java.nio.charset.Charset;


public abstract class AbstractPowerShellExecutor extends AbstractScriptExecutor {

    @Override
    protected String getScriptName(BigInteger jobId) {
        return String.format("powershell_%d.ps1", jobId);
    }

    @Override
    protected String getRunCommand() {
        return "powershell.exe";
    }

    @Override
    protected Charset getCharset() {
        return Charset.defaultCharset();
    }

    @Override
    protected ProcessBuilder getScriptProcessBuilder(String scriptPath) {
        return new ProcessBuilder(getRunCommand(), "-ExecutionPolicy", "Bypass", "-File", scriptPath);
    }
}
