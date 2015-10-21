package com.ft.methodeapi.service.methode;

import com.ft.methodeapi.service.methode.monitoring.GaugeRangeHealthCheck;
import com.codahale.metrics.Gauge;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * GaugeRangeHealthCheckTest
 *
 * @author Simon.Gibbs
 */
public class GaugeRangeHealthCheckTest {
    private static final HealthcheckParameters PARAMS = new HealthcheckParameters(
            "Test Health Check", 3, "Business Impact", "Technical Impact", "http://panic.example.org/"
            );
    
    Gauge<Integer> fixedValueGauge = new Gauge<Integer>() {
        @Override
        public Integer getValue() {
            return 1000;
        }
    };

    @Test
    public void shouldAlertWhenGaugeIsHigh() {
        GaugeRangeHealthCheck check = new GaugeRangeHealthCheck<>(PARAMS, fixedValueGauge,1, 500);
        assertThat(check.execute().isHealthy(),is(false));
    }


    @Test
    public void shouldNotAlertWhenGaugeIsInMiddleOfRange() {
        GaugeRangeHealthCheck check = new GaugeRangeHealthCheck<>(PARAMS, fixedValueGauge,1, 2000);
        assertThat(check.execute().isHealthy(), is(true));
    }

    @Test
    public void shouldAlertWhenGaugeIsLow() {
        GaugeRangeHealthCheck check = new GaugeRangeHealthCheck<>(PARAMS, fixedValueGauge,2000, 4000);
        assertThat(check.execute().isHealthy(), is(false));
    }

    @Test
    public void shouldNotPutNameInMessage() {
        GaugeRangeHealthCheck check = new GaugeRangeHealthCheck<>(PARAMS, fixedValueGauge,2000, 4000);
        assertThat(check.execute().getMessage(), not(containsString(PARAMS.getName())));
    }

}
