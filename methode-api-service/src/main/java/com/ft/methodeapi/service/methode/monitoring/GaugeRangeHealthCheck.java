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
            return report("snapshot > max: " + snapshotValue + " > " + max);
        }

        if(snapshotValue < min) {
            return report("snapshot < min: " + snapshotValue + " < " + min);
        }

        return Result.healthy(min + " <= " + snapshotValue + " <= " + max);
    }

    private Result report(String message) {
        LOGGER.warn(this.getName() + ": " + message); // use WARN to prevent duplicate alerts
        return Result.unhealthy(message);
    }
}
