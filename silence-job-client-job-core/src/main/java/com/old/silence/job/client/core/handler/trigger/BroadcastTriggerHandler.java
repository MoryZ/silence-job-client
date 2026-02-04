package com.old.silence.job.client.core.handler.trigger;


import java.math.BigInteger;

public class BroadcastTriggerHandler extends TriggerJobHandler<BroadcastTriggerHandler>{

    public BroadcastTriggerHandler(BigInteger triggerJobId) {
        super(triggerJobId);
    }

    @Override
    public BroadcastTriggerHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}
