package com.ft.methodeapi.service.methode.connection;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.health.HealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DeallocationQueueSizeHealthCheck
 *
 * @author Simon.Gibbs
 */
public class DeallocationQueueSizeHealthCheck extends HealthCheck {


    private static final Logger LOGGER = LoggerFactory.getLogger(DeallocationQueueSizeHealthCheck.class);

    private final String name;
    private Gauge<Integer> deallocationQueueLength;
    private int alertThreshold;

    public DeallocationQueueSizeHealthCheck(String connectionName, Gauge<Integer> deallocationQueueLength, int alertThreshold) {
        this.name = connectionName;
        this.deallocationQueueLength = deallocationQueueLength;
        this.alertThreshold = alertThreshold;
    }

    @Override
    protected Result check() throws Exception {

        int measurement = deallocationQueueLength.getValue();

        if(measurement>=alertThreshold) {
            String message = String.format("More than queue_size_threshold=%d connections await deallocation. actual_queue_size=%d",alertThreshold,measurement);
            LOGGER.warn(String.format("%s: %s", this.getName(), message));
            return Result.unhealthy(message);
        }

        return Result.healthy(String.format("queue_size_threshold=%d , actual_queue_size=%d",alertThreshold,measurement));
    }
    
    private String getName() {
        return name;
    }
}
