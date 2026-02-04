package com.old.silence.job.client.common.rpc.supports.handler;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.ServiceLoaderUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.client.common.HandlerInterceptor;
import com.old.silence.job.client.common.cache.EndPointInfoCache;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.common.exception.SilenceJobClientException;
import com.old.silence.job.client.common.rpc.client.RequestMethod;
import com.old.silence.job.client.common.rpc.supports.http.HttpRequest;
import com.old.silence.job.client.common.rpc.supports.http.HttpResponse;
import com.old.silence.job.client.common.rpc.supports.scan.EndPointInfo;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.grpc.auto.GrpcSilenceJobRequest;
import com.old.silence.job.common.grpc.auto.Metadata;
import com.old.silence.job.common.model.ApiResult;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.common.util.JsonUtils;
import com.old.silence.job.log.SilenceJobLog;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
public class SilenceDispatcherRequestHandler {
    private final SilenceJobProperties silenceJobProperties;

    public SilenceDispatcherRequestHandler(SilenceJobProperties silenceJobProperties) {
        this.silenceJobProperties = silenceJobProperties;
    }

    public SilenceJobRpcResult dispatch(NettyHttpRequest request) {

        SilenceJobRpcResult silenceJobRpcResult = new SilenceJobRpcResult();

        List<HandlerInterceptor> handlerInterceptors = handlerInterceptors();
        SilenceJobRequest retryRequest = JSON.parseObject(request.getContent(), SilenceJobRequest.class);
        HttpRequest httpRequest = request.getHttpRequest();
        HttpResponse httpResponse = request.getHttpResponse();
        EndPointInfo endPointInfo = null;
        ApiResult resultObj = null;
        Exception e = null;
        try {
            String silenceJobAuth = request.getHeaders().getAsString(SystemConstants.SILENCE_JOB_AUTH_TOKEN);
            String configToken = Optional.ofNullable(silenceJobProperties.getToken()).orElse(SystemConstants.DEFAULT_TOKEN);
            if (!configToken.equals(silenceJobAuth)) {
                throw new SilenceJobClientException("认证失败.【请检查配置的Token是否正确】");
            }

            UrlBuilder builder = UrlBuilder.ofHttp(request.getUri());
            RequestMethod requestMethod = RequestMethod.valueOf(request.getMethod().name());

            endPointInfo = EndPointInfoCache.get(builder.getPathStr(), requestMethod);
            if (Objects.isNull(endPointInfo)) {
                throw new SilenceJobClientException("无法找到对应的处理请检查对应的包是否正确引入. " +
                        "path:[{}] requestMethod:[{}]", builder.getPathStr(), requestMethod);
            }

            Class<?>[] paramTypes = endPointInfo.getMethod().getParameterTypes();
            Object[] args = retryRequest.getArgs();

            Object[] deSerialize = (Object[]) deSerialize(JSON.toJSONString(args), endPointInfo.getMethod(),
                    httpRequest, httpResponse);

            for (HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                if (!handlerInterceptor.preHandle(httpRequest, httpResponse, endPointInfo)) {
                    return silenceJobRpcResult;
                }
            }

            if (paramTypes.length > 0) {
                resultObj = (ApiResult) ReflectionUtils.invokeMethod(endPointInfo.getMethod(),
                        endPointInfo.getExecutor(), deSerialize);
            } else {
                resultObj = (ApiResult) ReflectionUtils.invokeMethod(endPointInfo.getMethod(),
                        endPointInfo.getExecutor());
            }

            for (HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                handlerInterceptor.postHandle(httpRequest, httpResponse, endPointInfo);
            }
        } catch (Exception ex) {
            SilenceJobLog.LOCAL.error("http request error. [{}]", request.getContent(), ex);
            silenceJobRpcResult.setMessage(ex.getMessage());
            silenceJobRpcResult.setCode(500);
            e = ex;
        } finally {
            silenceJobRpcResult.setReqId(retryRequest.getReqId());
            if (Objects.nonNull(resultObj)) {
                silenceJobRpcResult.setData(resultObj.getData());
                silenceJobRpcResult.setMessage(resultObj.getMessage());
                silenceJobRpcResult.setCode(resultObj.getCode());
            }

            for (HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                handlerInterceptor.afterCompletion(httpRequest, httpResponse, endPointInfo, e);
            }
        }

        return silenceJobRpcResult;
    }

    public SilenceJobRpcResult dispatch(GrpcRequest request) {
        SilenceJobRpcResult silenceJobRpcResult = new SilenceJobRpcResult();

        HttpRequest httpRequest = request.getHttpRequest();
        HttpResponse httpResponse = request.getHttpResponse();

        List<HandlerInterceptor> handlerInterceptors = handlerInterceptors();

        GrpcSilenceJobRequest silenceJobRequest = request.getSilenceJobRequest();
        EndPointInfo endPointInfo = null;
        ApiResult resultObj = null;
        Exception e = null;
        try {
            Metadata metadata = silenceJobRequest.getMetadata();
            Map<String, String> headersMap = metadata.getHeadersMap();
            String silenceJobAuth = headersMap.get(SystemConstants.SILENCE_JOB_AUTH_TOKEN);
            String configToken = Optional.ofNullable(silenceJobProperties.getToken()).orElse(SystemConstants.DEFAULT_TOKEN);
            if (!configToken.equals(silenceJobAuth)) {
                throw new SilenceJobClientException("认证失败.【请检查配置的Token是否正确】");
            }

            UrlBuilder builder = UrlBuilder.ofHttp(httpRequest.getUri());
            endPointInfo = EndPointInfoCache.get(builder.getPathStr(), RequestMethod.POST);
            if (Objects.isNull(endPointInfo)) {
                throw new SilenceJobClientException("无法找到对应的处理请检查对应的包是否正确引入. " +
                                                  "path:[{}] requestMethod:[{}]", builder.getPathStr());
            }

            Class<?>[] paramTypes = endPointInfo.getMethod().getParameterTypes();
            GrpcSilenceJobRequest grpcSilenceJobRequest = request.getSilenceJobRequest();
            Object[] args = JSON.parseObject(grpcSilenceJobRequest.getBody(), Object[].class);

            Object[] deSerialize = (Object[]) deSerialize(JSON.toJSONString(args), endPointInfo.getMethod(),
                httpRequest, httpResponse);

            for (HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                if (!handlerInterceptor.preHandle(httpRequest, httpResponse, endPointInfo)) {
                    return silenceJobRpcResult;
                }
            }

            if (paramTypes.length > 0) {
                resultObj = (ApiResult) ReflectionUtils.invokeMethod(endPointInfo.getMethod(),
                    endPointInfo.getExecutor(), deSerialize);
            } else {
                resultObj = (ApiResult) ReflectionUtils.invokeMethod(endPointInfo.getMethod(),
                    endPointInfo.getExecutor());
            }

            for (HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                handlerInterceptor.postHandle(httpRequest, httpResponse, endPointInfo);
            }
        } catch (Exception ex) {
            SilenceJobLog.LOCAL.error("http request error. [{}]", silenceJobRequest, ex);
            silenceJobRpcResult.setMessage(ex.getMessage());
            silenceJobRpcResult.setCode(500);
            e = ex;
        } finally {
            silenceJobRpcResult.setReqId(0);
            if (Objects.nonNull(resultObj)) {
                silenceJobRpcResult.setData(resultObj.getData());
                silenceJobRpcResult.setMessage(resultObj.getMessage());
                silenceJobRpcResult.setCode(resultObj.getCode());
            }

            for (HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                handlerInterceptor.afterCompletion(httpRequest, httpResponse, endPointInfo, e);
            }
        }

        return silenceJobRpcResult;
    }

    private static List<HandlerInterceptor> handlerInterceptors() {
        List<HandlerInterceptor> handlerInterceptors = ServiceLoaderUtil.loadList(HandlerInterceptor.class);
        if (CollectionUtils.isEmpty(handlerInterceptors)) {
            return Collections.emptyList();
        }

        return handlerInterceptors.stream().sorted(Comparator.comparingInt(HandlerInterceptor::order)).collect(
                Collectors.toList());
    }

    public Object deSerialize(String infoStr, Method method,
                              HttpRequest httpRequest, HttpResponse httpResponse) throws JsonProcessingException {

        Type[] paramTypes = method.getGenericParameterTypes();
        Parameter[] parameters = method.getParameters();
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
                continue;
            }

            Parameter parameter = parameters[i];
            if (parameter.getType().isAssignableFrom(HttpRequest.class)) {
                params[i] = httpRequest;
                continue;
            }

            if (parameter.getType().isAssignableFrom(HttpResponse.class)) {
                params[i] = httpResponse;
            }
        }

        return params;
    }

}
