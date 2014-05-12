package com.ft.methodeapi.service.methode;

import com.ft.methodeapi.service.methode.monitoring.GaugeRangeHealthCheck;
import com.yammer.metrics.core.Gauge;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * GaugeRangeHealthCheckTest
 *
 * @author Simon.Gibbs
 */
public class GaugeRangeHealthCheckTest {

    Gauge<Integer> fixedValueGauge = new Gauge<Integer>() {
        @Override
        public Integer value() {
            return 1000;
        }
    };

    @Test
    public void shouldAlertWhenGaugeIsHigh() {
        GaugeRangeHealthCheck check = new GaugeRangeHealthCheck<>("Foo", fixedValueGauge,1, 500);
        assertThat(check.execute().isHealthy(),is(false));
    }


    @Test
    public void shouldNotAlertWhenGaugeIsInMiddleOfRange() {
        GaugeRangeHealthCheck check = new GaugeRangeHealthCheck<>("Foo", fixedValueGauge,1, 2000);
        assertThat(check.execute().isHealthy(), is(true));
    }

    @Test
    public void shouldAlertWhenGaugeIsLow() {
        GaugeRangeHealthCheck check = new GaugeRangeHealthCheck<>("Foo", fixedValueGauge,2000, 4000);
        assertThat(check.execute().isHealthy(), is(false));
    }

}
