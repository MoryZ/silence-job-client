package com.old.silence.job.client.common.cache;


import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.RetryNotifyScene;
import com.old.silence.job.common.dto.ConfigDTO;

import java.util.List;
import java.util.Objects;



public final class GroupVersionCache {
    private GroupVersionCache() {
    }

    private static ConfigDTO CONFIG;

    public static void setConfig(ConfigDTO config) {
        GroupVersionCache.CONFIG = config;
    }

    public static Integer getVersion() {
        if (Objects.isNull(CONFIG)) {
            return 0;
        }
        return CONFIG.getVersion();
    }

    public static long getDdl(String sceneName) {
        // 缓存初始化时configDTO值为null,可能造成空指针异常
        if (Objects.isNull(CONFIG)) {
            return SystemConstants.DEFAULT_DDL;
        }
        List<ConfigDTO.Scene> sceneList = CONFIG.getSceneList();
        if (CollectionUtils.isEmpty(sceneList)) {
            return SystemConstants.DEFAULT_DDL;
        }

        for (ConfigDTO.Scene scene : sceneList) {
            if (scene.getSceneName().equals(sceneName)) {
                return scene.getDdl();
            }
        }

        return SystemConstants.DEFAULT_DDL;
    }

    public static ConfigDTO.Notify getRetryNotifyAttribute(RetryNotifyScene notifyScene) {
        List<ConfigDTO.Notify> notifyList = CONFIG.getNotifyList();
        if (CollectionUtils.isNotEmpty(notifyList)) {
            for (ConfigDTO.Notify notify : notifyList) {
                if (Objects.nonNull(notify.getRetryNotifyScene()) && notify.getRetryNotifyScene().equals(notifyScene)) {
                    return notify;
                }
            }
        }

        return null;
    }


    public static ConfigDTO.Notify getJobNotifyAttribute(JobNotifyScene notifyScene) {
        List<ConfigDTO.Notify> notifyList = CONFIG.getNotifyList();
        if (CollectionUtils.isNotEmpty(notifyList)) {
            for (ConfigDTO.Notify notify : notifyList) {
                if (Objects.nonNull(notify.getJobNotifyScene()) && notify.getJobNotifyScene().equals(notifyScene)) {
                    return notify;
                }
            }
        }

        return null;
    }

}
