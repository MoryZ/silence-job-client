package com.old.silence.job.client.retry.core.cache;

import com.google.common.util.concurrent.ListenableFuture;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public class FutureCache {

    private static final ConcurrentHashMap<BigInteger, ListenableFuture<?>> futureCache = new ConcurrentHashMap<>();

    public static void addFuture(BigInteger retryTaskId, ListenableFuture<?> future) {
        futureCache.put(retryTaskId, future);
    }

    public static void remove(BigInteger retryTaskId) {
        Optional.ofNullable(futureCache.get(retryTaskId)).ifPresent(future -> {
            future.cancel(true);
            futureCache.remove(retryTaskId);
        });
    }

}
