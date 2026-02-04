package com.old.silence.job.client.core.handler.trigger;


import java.math.BigInteger;

public class MapTriggerHandler extends TriggerJobHandler<MapTriggerHandler>{

    public MapTriggerHandler(BigInteger triggerJobId) {
        super(triggerJobId);
    }

    @Override
    public MapTriggerHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}
