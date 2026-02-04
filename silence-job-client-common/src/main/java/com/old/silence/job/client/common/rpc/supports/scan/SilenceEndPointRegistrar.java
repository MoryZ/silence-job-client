package com.old.silence.job.client.common.rpc.supports.scan;

import org.springframework.stereotype.Component;
import com.old.silence.job.client.common.Lifecycle;
import com.old.silence.job.client.common.cache.EndPointInfoCache;
import com.old.silence.job.client.common.exception.SilenceJobClientException;

import java.util.List;


@Component
public class SilenceEndPointRegistrar implements Lifecycle {
    private final SilenceEndPointScanner silenceEndPointScanner;

    public SilenceEndPointRegistrar(SilenceEndPointScanner silenceEndPointScanner) {
        this.silenceEndPointScanner = silenceEndPointScanner;
    }

    @Override
    public void start() {
        List<EndPointInfo> endPointInfos = silenceEndPointScanner.doScan();
        for (EndPointInfo endPointInfo : endPointInfos) {
            if (EndPointInfoCache.isExisted(endPointInfo.getPath(), endPointInfo.getRequestMethod())) {
                throw new SilenceJobClientException("Duplicate endpoint path: {}", endPointInfo.getPath());
            }

            EndPointInfoCache.put(endPointInfo);
        }

    }

    @Override
    public void close() {

    }
}
