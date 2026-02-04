package com.old.silence.job.client.core.timer;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.TimeUnit;


public class TimerManager {

    private static final HashedWheelTimer wheelTimer;

    static {
        wheelTimer = new HashedWheelTimer(
                new CustomizableThreadFactory("job-task-timer-wheel-"), 1,
                TimeUnit.SECONDS, 1024);
    }

    private TimerManager() {
    }

    public static Timeout add(TimerTask task, long delay, TimeUnit unit) {
        return wheelTimer.newTimeout(task, delay, unit);
    }
}
