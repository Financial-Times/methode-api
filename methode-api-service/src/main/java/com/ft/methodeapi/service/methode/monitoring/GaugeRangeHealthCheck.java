package com.ft.methodeapi.service.methode.monitoring;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.HealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GaugeRangeHealthCheck
 *
 * @author Simon.Gibbs
 */
public class GaugeRangeHealthCheck<N extends Number, G extends Gauge<N>> extends HealthCheck {

    private final long max;
    private final long min;
    private final Gauge<N> gauge;

    private final Logger LOGGER = LoggerFactory.getLogger(GaugeRangeHealthCheck.class);


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
            String message = String.format("%d > %d",snapshotValue,max);
            LOGGER.warn(this.getName() + ": " +message); // use WARN to prevent duplicate alerts
            return Result.unhealthy(this.getName() + ": " + message);
        }

        if(snapshotValue < min) {
            String message = String.format("%d < %d",snapshotValue,min);
            LOGGER.warn(this.getName() + ": " + message); // use WARN to prevent duplicate alerts
            return Result.unhealthy(message);
        }

        return Result.healthy(String.format("%d <= %d <= %d",min, snapshotValue,max));
    }
}
