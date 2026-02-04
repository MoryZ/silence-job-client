package com.old.silence.job.client.retry.core.generator;

import cn.hutool.core.util.StrUtil;
import com.google.common.hash.Hashing;
import com.old.silence.job.client.retry.core.IdempotentIdGenerate;
import com.old.silence.job.common.model.IdempotentIdContext;

import java.nio.charset.StandardCharsets;

/**
 * 默认的idempotentId 生成器
 *
 */
public class SimpleIdempotentIdGenerate implements IdempotentIdGenerate {

    @Override
    public String idGenerate(IdempotentIdContext context) throws Exception {
        String str = context.toString();
        if (StrUtil.isBlankIfStr(str)) {
            return StrUtil.EMPTY;
        }

        return Hashing.sha256().hashBytes(str.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
