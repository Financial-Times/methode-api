package com.ft.methodeapi.service.methode.monitoring;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.HealthCheck;

/**
 * GaugeTooLargeHealthCheck
 *
 * @author Simon.Gibbs
 */
public class GaugeTooLargeHealthCheck<T extends Number, G extends Gauge<T>> extends HealthCheck {

    private final long max;
    private final Gauge<? extends Number> gauge;

    public GaugeTooLargeHealthCheck(String name, G gauge, T maxValue) {
        super(name);
        this.gauge = gauge;
        this.max = maxValue.longValue();
    }

    @Override
    protected Result check() throws Exception {

        long snapshotValue = gauge.value().longValue();

        if(snapshotValue > max) {
            Result.unhealthy(String.format("%d > %d",snapshotValue,max));
        }

        return Result.healthy(String.format("%d < %d",snapshotValue,max));
    }
}
