package com.old.silence.job.client.common.cache;

import cn.hutool.core.lang.Pair;
import com.old.silence.job.client.common.rpc.client.RequestMethod;
import com.old.silence.job.client.common.rpc.supports.scan.EndPointInfo;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public final class EndPointInfoCache {

    private EndPointInfoCache() {
    }

    private static final ConcurrentHashMap<Pair<String, RequestMethod>, EndPointInfo> ENDPOINT_REPOSITORY = new ConcurrentHashMap<>();

    public static void put(EndPointInfo endPointInfo) {
        ENDPOINT_REPOSITORY.put(Pair.of(endPointInfo.getPath(), endPointInfo.getRequestMethod()), endPointInfo);
    }

    public static EndPointInfo get(String path, RequestMethod method) {
        return ENDPOINT_REPOSITORY.get(Pair.of(path, method));
    }

    public static boolean isExisted(String path, RequestMethod method) {
        return Objects.nonNull(ENDPOINT_REPOSITORY.get(Pair.of(path, method)));
    }

}
