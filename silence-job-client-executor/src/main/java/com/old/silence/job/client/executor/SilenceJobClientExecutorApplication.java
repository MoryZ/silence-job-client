package com.old.silence.job.client.executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SilenceJob Client Executor Application
 * 独立的客户端执行器应用，用于分布式任务执行
 */
@SpringBootApplication
public class SilenceJobClientExecutorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SilenceJobClientExecutorApplication.class, args);
    }
}