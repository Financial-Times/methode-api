package com.ft.methodeapi.service.methode.monitoring;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.HealthCheck;

/**
 * GaugeRangeHealthCheck
 *
 * @author Simon.Gibbs
 */
public class GaugeRangeHealthCheck<N extends Number, G extends Gauge<N>> extends HealthCheck {

    private final long max;
    private final long min;
    private final Gauge<N> gauge;


    public GaugeRangeHealthCheck(String name, G gauge, N minValue, N maxValue) {
        super(name);
        this.gauge = gauge;
        this.max = maxValue.longValue();
        this.min = minValue.longValue();
    }

    @Override
    protected Result check() throws Exception {

        long snapshotValue = gauge.value().longValue();

        if(snapshotValue > max) {
            return Result.unhealthy(String.format("%d > %d",snapshotValue,max));
        }

        if(snapshotValue < min) {
            return Result.unhealthy(String.format("%d < %d",snapshotValue,min));
        }

        return Result.healthy(String.format("%d <= %d <= %d",min, snapshotValue,max));
    }
}
