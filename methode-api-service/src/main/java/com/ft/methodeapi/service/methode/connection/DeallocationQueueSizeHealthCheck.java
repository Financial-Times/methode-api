package com.ft.methodeapi.service.methode.connection;

import com.codahale.metrics.Gauge;
import com.ft.methodeapi.service.methode.HealthcheckParameters;
import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DeallocationQueueSizeHealthCheck
 *
 * @author Simon.Gibbs
 */
public class DeallocationQueueSizeHealthCheck
        extends AdvancedHealthCheck {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DeallocationQueueSizeHealthCheck.class);
    
    private final HealthcheckParameters params;
    
    private Gauge<Integer> deallocationQueueLength;
    private int alertThreshold;

    public DeallocationQueueSizeHealthCheck(HealthcheckParameters params, Gauge<Integer> deallocationQueueLength, int alertThreshold) {
        super(params.getName());
        this.params = params;
        this.deallocationQueueLength = deallocationQueueLength;
        this.alertThreshold = alertThreshold;
    }

    @Override
    protected AdvancedResult checkAdvanced() throws Exception {

        int measurement = deallocationQueueLength.getValue();

        if(measurement>=alertThreshold) {
            String message = String.format("More than queue_size_threshold=%d connections await deallocation. actual_queue_size=%d",alertThreshold,measurement);
            LOGGER.warn(String.format("%s: %s", this.getName(), message));
            return AdvancedResult.error(this, message);
        }

        return AdvancedResult.healthy(String.format("queue_size_threshold=%d , actual_queue_size=%d",alertThreshold,measurement));
    }

    @Override
    protected String businessImpact() {
        return params.getBusinessImpact();
    }

    @Override
    protected String panicGuideUrl() {
        return params.getPanicGuideUrl();
    }

    @Override
    protected int severity() {
        return params.getSeverity();
    }

    @Override
    protected String technicalSummary() {
        return params.getTechnicalSummary();
    }
}
