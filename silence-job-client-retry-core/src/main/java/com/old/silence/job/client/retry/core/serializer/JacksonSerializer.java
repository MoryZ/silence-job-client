package com.old.silence.job.client.retry.core.serializer;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.old.silence.job.client.retry.core.RetryArgSerializer;
import com.old.silence.job.common.util.JsonUtils;
import com.old.silence.job.log.SilenceJobLog;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Jackson序列化
 *
 */

public class JacksonSerializer implements RetryArgSerializer {

    @Override
    public String serialize(Object serializeInfo) {
        return JSON.toJSONString(serializeInfo);
    }

    @Override
    public Object deSerialize(String infoStr, Class tClass, Method method) throws JsonProcessingException {

        Type[] paramTypes = method.getGenericParameterTypes();

        Object[] params = new Object[paramTypes.length];

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = JsonUtils.toJson(infoStr);
        if (Objects.isNull(jsonNode)) {
            SilenceJobLog.LOCAL.warn("jsonNode is null. infoStr:[{}]", infoStr);
            return params;
        }

        for (int i = 0; i < paramTypes.length; i++) {
            JsonNode node = jsonNode.get(i);
            if (Objects.nonNull(node)) {
                params[i] = mapper.readValue(node.toString(), mapper.constructType(paramTypes[i]));
            }
        }

        return params;
    }
}
