package com.old.silence.job.client.common.client;

import com.old.silence.job.client.common.annotation.Mapping;
import com.old.silence.job.client.common.annotation.SilenceEndPoint;
import com.old.silence.job.client.common.cache.GroupVersionCache;
import com.old.silence.job.client.common.rpc.client.RequestMethod;
import com.old.silence.job.common.dto.ConfigDTO;

import static com.old.silence.job.common.constant.SystemConstants.HTTP_PATH.SYNC_CONFIG;

/**
 * SilenceJob 通用EndPoint
 *
 */
@SilenceEndPoint
public class SilenceJobCommonEndPoint {

    /**
     * 同步版本
     */
    @Mapping(path = SYNC_CONFIG, method = RequestMethod.POST)
    public void syncVersion(ConfigDTO configDTO) {
        GroupVersionCache.setConfig(configDTO);
    }

}
