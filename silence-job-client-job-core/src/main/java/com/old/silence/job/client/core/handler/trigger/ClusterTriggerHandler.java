package com.old.silence.job.client.core.handler.trigger;


import java.math.BigInteger;

public class ClusterTriggerHandler extends TriggerJobHandler<ClusterTriggerHandler>{

    public ClusterTriggerHandler(BigInteger triggerJobId) {
        super(triggerJobId);
    }

    @Override
    public ClusterTriggerHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}
