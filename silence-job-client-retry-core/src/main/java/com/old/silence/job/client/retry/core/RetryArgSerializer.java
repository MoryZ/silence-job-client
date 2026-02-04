package com.old.silence.job.client.retry.core;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.lang.reflect.Method;


public interface RetryArgSerializer {

    String serialize(Object serializeInfo);

    Object deSerialize(String infoStr, Class tClass, Method method) throws JsonProcessingException;
}
