package com.old.silence.job.client.retry.core.client;

import cn.hutool.core.lang.Assert;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.old.silence.job.client.common.Lifecycle;
import com.old.silence.job.client.common.annotation.Mapping;
import com.old.silence.job.client.common.annotation.SilenceEndPoint;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.common.log.support.SilenceJobLogManager;
import com.old.silence.job.client.common.rpc.client.RequestMethod;
import com.old.silence.job.client.retry.core.IdempotentIdGenerate;
import com.old.silence.job.client.retry.core.RetryArgSerializer;
import com.old.silence.job.client.retry.core.cache.FutureCache;
import com.old.silence.job.client.retry.core.cache.RetryerInfoCache;
import com.old.silence.job.client.retry.core.callback.future.CallbackTaskExecutorFutureCallback;
import com.old.silence.job.client.retry.core.callback.future.RetryTaskExecutorFutureCallback;
import com.old.silence.job.client.retry.core.context.CallbackContext;
import com.old.silence.job.client.retry.core.context.RemoteRetryContext;
import com.old.silence.job.client.retry.core.exception.SilenceRetryClientException;
import com.old.silence.job.client.retry.core.executor.RemoteCallbackExecutor;
import com.old.silence.job.client.retry.core.executor.RemoteRetryExecutor;
import com.old.silence.job.client.retry.core.loader.SilenceRetrySpiLoader;
import com.old.silence.job.client.retry.core.log.RetryLogMeta;
import com.old.silence.job.client.retry.core.retryer.RetryerInfo;
import com.old.silence.job.client.retry.core.serializer.JacksonSerializer;
import com.old.silence.job.client.retry.core.timer.StopTaskTimerTask;
import com.old.silence.job.client.retry.core.timer.TimerManager;
import com.old.silence.job.common.client.dto.DispatchRetryResultDTO;
import com.old.silence.job.common.client.dto.GenerateRetryIdempotentIdDTO;
import com.old.silence.job.common.client.dto.request.DispatchRetryRequest;
import com.old.silence.job.common.client.dto.request.RetryCallbackRequest;
import com.old.silence.job.common.client.dto.request.StopRetryRequest;
import com.old.silence.job.common.model.ApiResult;
import com.old.silence.job.common.model.IdempotentIdContext;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.log.enums.LogTypeEnum;

import jakarta.validation.Valid;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.old.silence.job.common.constant.SystemConstants.HTTP_PATH.RETRY_CALLBACK;
import static com.old.silence.job.common.constant.SystemConstants.HTTP_PATH.RETRY_DISPATCH;
import static com.old.silence.job.common.constant.SystemConstants.HTTP_PATH.RETRY_GENERATE_IDEM_ID;
import static com.old.silence.job.common.constant.SystemConstants.HTTP_PATH.RETRY_STOP;

/**
 * 服务端调调用客户端进行重试流量下发、配置变更通知等操作
 *
 */
@SilenceEndPoint
@Validated
public class SilenceRetryEndPoint implements Lifecycle {
    private final RemoteRetryExecutor remoteRetryExecutor;
    private final RemoteCallbackExecutor remoteCallbackExecutor;
    private final SilenceJobProperties silenceJobProperties;
    private ThreadPoolExecutor dispatcherThreadPool = null;

    public SilenceRetryEndPoint(RemoteRetryExecutor remoteRetryExecutor, RemoteCallbackExecutor remoteCallbackExecutor, SilenceJobProperties silenceJobProperties) {
        this.remoteRetryExecutor = remoteRetryExecutor;
        this.remoteCallbackExecutor = remoteCallbackExecutor;
        this.silenceJobProperties = silenceJobProperties;
    }

    /**
     * 服务端调度重试入口
     */
    @Mapping(path = RETRY_DISPATCH, method = RequestMethod.POST)
    public ApiResult<Boolean> dispatch(@Valid DispatchRetryRequest request) {

        RemoteRetryContext retryContext = bulidRemoteRetryContext(request);

        RetryerInfo retryerInfo = RetryerInfoCache.get(request.getSceneName(), request.getExecutorName());
        if (Objects.isNull(retryerInfo)) {
            SilenceJobLog.REMOTE.error("场景:[{}]配置不存在, 请检查您的场景和执行器是否存在", request.getSceneName());
            return new ApiResult<>(500, MessageFormat.format("场景:[{0}]配置不存在, 请检查您的场景和执行器是否存在", request.getSceneName()));
        }

        // 初始化实时日志上下文
        initLogContext(retryContext);

        RetryArgSerializer retryArgSerializer = SilenceRetrySpiLoader.loadRetryArgSerializer();

        Object[] deSerialize;
        try {
            deSerialize = (Object[]) retryArgSerializer.deSerialize(request.getArgsStr(),
                    retryerInfo.getExecutor().getClass(), retryerInfo.getMethod());
        } catch (JsonProcessingException e) {
            SilenceJobLog.REMOTE.error("参数解析异常 args:[{}]", request.getArgsStr(), e);
            return new ApiResult<>(500, MessageFormat.format("参数解析异常 args:[{0}]", request.getArgsStr()));
        }

        retryContext.setDeSerialize(deSerialize);

        ListeningExecutorService decorator = MoreExecutors.listeningDecorator(dispatcherThreadPool);
        ListenableFuture<DispatchRetryResultDTO> submit = decorator.submit(() -> remoteRetryExecutor.doRetry(retryContext));

        FutureCache.addFuture(request.getRetryTaskId(), submit);
        Futures.addCallback(submit, new RetryTaskExecutorFutureCallback(retryContext), decorator);

        // 将任务添加到时间轮中，到期停止任务
        TimerManager.add(new StopTaskTimerTask(request.getRetryTaskId()), request.getExecutorTimeout(), TimeUnit.SECONDS);

        SilenceJobLog.REMOTE.info("重试任务:[{}] 调度成功. ", request.getRetryTaskId());

        return new ApiResult<>(Boolean.TRUE);
    }

    private static RemoteRetryContext bulidRemoteRetryContext(DispatchRetryRequest request) {
        RemoteRetryContext retryContext = new RemoteRetryContext();
        retryContext.setRetryTaskId(request.getRetryTaskId());
        retryContext.setRetryId(request.getRetryId());
        retryContext.setRetryCount(request.getRetryCount());
        retryContext.setArgsStr(request.getArgsStr());
        retryContext.setGroupName(request.getGroupName());
        retryContext.setNamespaceId(request.getNamespaceId());
        retryContext.setScene(request.getSceneName());
        retryContext.setExecutorName(request.getExecutorName());
        return retryContext;
    }

    private static void initLogContext(RemoteRetryContext context) {
        RetryLogMeta retryLogMeta = new RetryLogMeta();
        retryLogMeta.setGroupName(context.getGroupName());
        retryLogMeta.setNamespaceId(context.getNamespaceId());
        retryLogMeta.setRetryId(context.getRetryId());
        retryLogMeta.setRetryTaskId(context.getRetryTaskId());
        SilenceJobLogManager.initLogInfo(retryLogMeta, LogTypeEnum.RETRY);
    }

    @Mapping(path = RETRY_CALLBACK, method = RequestMethod.POST)
    public ApiResult<Boolean> callback(@Valid RetryCallbackRequest callbackDTO) {
        CallbackContext callbackContext = buildCallbackContext(callbackDTO);

        try {
            initLogContext(callbackContext);

            RetryerInfo retryerInfo = RetryerInfoCache.get(callbackDTO.getSceneName(), callbackDTO.getExecutorName());
            if (Objects.isNull(retryerInfo)) {
                SilenceJobLog.REMOTE.error("场景:[{}]配置不存在, 请检查您的场景和执行器是否存在", callbackDTO.getSceneName());
                return new ApiResult<>(500, "回调失败", Boolean.FALSE);
            }

            RetryArgSerializer retryArgSerializer = SilenceRetrySpiLoader.loadRetryArgSerializer();

            Object[] deSerialize  = (Object[]) retryArgSerializer.deSerialize(callbackDTO.getArgsStr(),
                    retryerInfo.getExecutor().getClass(), retryerInfo.getMethod());
            callbackContext.setDeSerialize(deSerialize);
            callbackContext.setRetryerInfo(retryerInfo);
        } catch (JsonProcessingException e) {
            SilenceJobLog.REMOTE.error("参数解析异常", e);
            return new ApiResult<>(500, "回调失败", Boolean.FALSE);
        }

        ListeningExecutorService decorator = MoreExecutors.listeningDecorator(dispatcherThreadPool);
        ListenableFuture<Boolean> submit = decorator.submit(() -> {
            remoteCallbackExecutor.doRetryCallback(callbackContext);
            return Boolean.TRUE;
        });

        FutureCache.addFuture(callbackDTO.getRetryTaskId(), submit);
        Futures.addCallback(submit, new CallbackTaskExecutorFutureCallback(callbackContext), decorator);

        // 将任务添加到时间轮中，到期停止任务
        TimerManager.add(new StopTaskTimerTask(callbackDTO.getRetryTaskId()), callbackDTO.getExecutorTimeout(), TimeUnit.SECONDS);

        SilenceJobLog.REMOTE.info("回调任务:[{}] 调度成功. ", callbackDTO.getRetryTaskId());
        return new ApiResult<>(Boolean.TRUE);
    }

    private static CallbackContext buildCallbackContext(RetryCallbackRequest callbackDTO) {
        CallbackContext callbackContext = new CallbackContext();
        callbackContext.setRetryTaskId(callbackDTO.getRetryTaskId());
        callbackContext.setRetryId(callbackDTO.getRetryId());
        callbackContext.setGroupName(callbackDTO.getGroupName());
        callbackContext.setNamespaceId(callbackDTO.getNamespaceId());
        callbackContext.setSceneName(callbackDTO.getSceneName());
        callbackContext.setRetryStatus(callbackDTO.getRetryStatus());
        return callbackContext;
    }

    private static void initLogContext(CallbackContext context) {
        // 初始化实时日志上下文
        RetryLogMeta retryLogMeta = new RetryLogMeta();
        retryLogMeta.setGroupName(context.getGroupName());
        retryLogMeta.setNamespaceId(context.getNamespaceId());
        retryLogMeta.setRetryTaskId(context.getRetryTaskId());
        retryLogMeta.setRetryId(context.getRetryId());
        SilenceJobLogManager.initLogInfo(retryLogMeta, LogTypeEnum.RETRY);
    }


    /**
     * 手动新增重试数据，模拟生成idempotentId
     *
     * @param generateRetryIdempotentIdDTO 生成idempotentId模型
     * @return idempotentId
     */
    @Mapping(path = RETRY_GENERATE_IDEM_ID, method = RequestMethod.POST)
    public ApiResult<String> idempotentIdGenerate(@Valid
                                                  GenerateRetryIdempotentIdDTO generateRetryIdempotentIdDTO) {

        String scene = generateRetryIdempotentIdDTO.getScene();
        String executorName = generateRetryIdempotentIdDTO.getExecutorName();
        String argsStr = generateRetryIdempotentIdDTO.getArgsStr();

        RetryerInfo retryerInfo = RetryerInfoCache.get(scene, executorName);
        Assert.notNull(retryerInfo,
                () -> new SilenceRetryClientException("重试信息不存在 scene:[{}] executorName:[{}]", scene, executorName));

        Method executorMethod = retryerInfo.getMethod();

        RetryArgSerializer retryArgSerializer = new JacksonSerializer();

        Object[] deSerialize = null;
        try {
            deSerialize = (Object[]) retryArgSerializer.deSerialize(argsStr, retryerInfo.getExecutor().getClass(),
                    retryerInfo.getMethod());
        } catch (JsonProcessingException e) {
            throw new SilenceRetryClientException("参数解析异常", e);
        }

        String idempotentId;
        try {
            Class<? extends IdempotentIdGenerate> idempotentIdGenerate = retryerInfo.getIdempotentIdGenerate();
            IdempotentIdGenerate generate = idempotentIdGenerate.getDeclaredConstructor().newInstance();
            Method method = idempotentIdGenerate.getMethod("idGenerate", IdempotentIdContext.class);
            IdempotentIdContext idempotentIdContext = new IdempotentIdContext(scene, executorName, deSerialize,
                    executorMethod.getName());
            idempotentId = (String) ReflectionUtils.invokeMethod(method, generate, idempotentIdContext);
        } catch (Exception exception) {
            SilenceJobLog.LOCAL.error("幂等id生成异常：{},{}", scene, argsStr, exception);
            throw new SilenceRetryClientException("idempotentId生成异常：{},{}", scene, argsStr);
        }

        return new ApiResult<>(idempotentId);
    }

    @Mapping(path = RETRY_STOP, method = RequestMethod.POST)
    public ApiResult<Boolean> stop(@Valid StopRetryRequest stopRetryRequest) {
        FutureCache.remove(stopRetryRequest.getRetryTaskId());
        return new ApiResult<>(Boolean.TRUE);
    }

    @Override
    public void start() {
        if (Objects.nonNull(dispatcherThreadPool)) {
            return;
        }

        SilenceJobProperties.ThreadPoolConfig threadPoolConfig = silenceJobProperties.getRetry().getDispatcherThreadPool();
        this.dispatcherThreadPool = new ThreadPoolExecutor(
                threadPoolConfig.getCorePoolSize(),
                threadPoolConfig.getMaximumPoolSize(),
                threadPoolConfig.getKeepAliveTime(),
                threadPoolConfig.getTimeUnit(),
                new LinkedBlockingQueue<>(threadPoolConfig.getQueueCapacity()),
                new CustomizableThreadFactory("silence-retry-dispatcher-"));
    }

    @Override
    public void close() {
        if (Objects.nonNull(dispatcherThreadPool)) {
            dispatcherThreadPool.shutdown();
        }
    }
}
