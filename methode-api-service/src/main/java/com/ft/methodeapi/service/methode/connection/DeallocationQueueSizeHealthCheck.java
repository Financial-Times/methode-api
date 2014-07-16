package com.ft.methodeapi.service.methode.connection;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.HealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DeallocationQueueSizeHealthCheck
 *
 * @author Simon.Gibbs
 */
public class DeallocationQueueSizeHealthCheck extends HealthCheck {


    private static final Logger LOGGER = LoggerFactory.getLogger(DeallocationQueueSizeHealthCheck.class);

    private Gauge<Integer> deallocationQueueLength;
    private int alertThreshold;


    public DeallocationQueueSizeHealthCheck(String connectionName, Gauge<Integer> deallocationQueueLength, int alertThreshold) {
        super(String.format("Deallocation Queue Size [connection=%s]",connectionName));
        this.deallocationQueueLength = deallocationQueueLength;
        this.alertThreshold = alertThreshold;
    }

    @Override
    protected Result check() throws Exception {

        int measurement = deallocationQueueLength.value();

        if(measurement>=alertThreshold) {
            String message = String.format("More than queue_size_threshold=%d connections await deallocation. actual_queue_size=%d",alertThreshold,measurement);
            LOGGER.warn(String.format("%s: %s", this.getName(), message));
            return Result.unhealthy(message);
        }

        return Result.healthy(String.format("queue_size_threshold=%d , actual_queue_size=%d",alertThreshold,measurement));
    }
}
