package com.old.silence.job.client.core.handler.trigger;


import java.math.BigInteger;

public class MapReduceTriggerHandler extends TriggerJobHandler<MapReduceTriggerHandler>{

    public MapReduceTriggerHandler(BigInteger triggerJobId) {
        super(triggerJobId);
    }

    @Override
    public MapReduceTriggerHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}
