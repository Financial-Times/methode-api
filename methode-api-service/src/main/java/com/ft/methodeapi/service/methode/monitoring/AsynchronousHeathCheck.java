package com.ft.methodeapi.service.methode.monitoring;

import com.yammer.metrics.core.HealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.GuardedBy;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AsynchronousHeathCheck
 *
 * @author Simon.Gibbs
 */
public class AsynchronousHeathCheck extends HealthCheck {


    private static final Logger LOGGER = LoggerFactory.getLogger(AsynchronousHeathCheck.class);

    @GuardedBy("this")
    private Result result = null;
    @GuardedBy("this")
    private long lastUpdate = System.currentTimeMillis();

    private long maxDelay;


    public AsynchronousHeathCheck(final HealthCheck target, ScheduledExecutorService executorService, long delay, TimeUnit unit) {
        super(target.getName());

        final Object monitor = this;

        executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                // run the check outside the synchronised block but update the value inside to ensure visibility
                Result newResult = safeExecute();
                synchronized (monitor) {
                     result = newResult;
                }
            }

            private Result safeExecute() {
                try {
                    return target.execute();
                } catch (Error error) {
                    // protect the task from being stopped, and seek human intervention
                    LOGGER.error("Caught serious issue (Java Error)",error);
                    return Result.unhealthy(error);
                } finally {
                    lastUpdate = System.currentTimeMillis();
                }
            }
        },0,delay,unit);

        maxDelay = unit.toMillis(delay) * 2;

    }

    @Override
    protected Result check() throws Exception {
        synchronized (this) {
            if((System.currentTimeMillis()-maxDelay)>lastUpdate) {
                LOGGER.warn("Healthcheck not invoked on schedule. name={}",this.getName());
                return Result.unhealthy("Not checked recently");
            }
            if(result==null) {
                return Result.healthy("Not yet checked");
            }
            return result;
        }
    }
}
