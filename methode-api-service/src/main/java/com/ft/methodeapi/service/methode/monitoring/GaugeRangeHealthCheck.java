package com.ft.methodeapi.service.methode.monitoring;

import com.codahale.metrics.Gauge;
import com.ft.methodeapi.service.methode.HealthcheckParameters;
import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GaugeRangeHealthCheck
 *
 * @author Simon.Gibbs
 */
public class GaugeRangeHealthCheck<N extends Number, G extends Gauge<N>>
        extends AdvancedHealthCheck {

    private final long max;
    private final long min;
    private final Gauge<N> gauge;

    private final Logger LOGGER = LoggerFactory.getLogger(GaugeRangeHealthCheck.class);
    
    private final HealthcheckParameters params;
    
    public GaugeRangeHealthCheck(HealthcheckParameters params, G gauge, N minValue, N maxValue) {
        super(params.getName());
        this.params = params;
        this.gauge = gauge;
        this.max = maxValue.longValue();
        this.min = minValue.longValue();
    }

    @Override
    protected AdvancedResult checkAdvanced() throws Exception {
        long snapshotValue = gauge.getValue().longValue();

        if(snapshotValue > max) {
            return report("snapshot > max: " + snapshotValue + " > " + max);
        }

        if(snapshotValue < min) {
            return report("snapshot < min: " + snapshotValue + " < " + min);
        }

        return AdvancedResult.healthy(min + " <= " + snapshotValue + " <= " + max);
    }

    private AdvancedResult report(String message) {
        LOGGER.warn(this.getName() + ": " + message); // use WARN to prevent duplicate alerts
        return AdvancedResult.error(this, message);
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
