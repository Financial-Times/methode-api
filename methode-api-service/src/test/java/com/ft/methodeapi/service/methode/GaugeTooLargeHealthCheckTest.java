package com.ft.methodeapi.service.methode;

import com.ft.methodeapi.service.methode.monitoring.GaugeTooLargeHealthCheck;
import com.yammer.metrics.core.Gauge;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * GaugeTooLargeHealthCheckTest
 *
 * @author Simon.Gibbs
 */
public class GaugeTooLargeHealthCheckTest {

    Gauge<Integer> fixedValueGauge = new Gauge<Integer>() {
        @Override
        public Integer value() {
            return 1000;
        }
    };

    @Test
    public void shouldAlertWhenGaugeIsHigh() {
        GaugeTooLargeHealthCheck check = new GaugeTooLargeHealthCheck<>("Foo", fixedValueGauge,500);
        assertThat(check.execute().isHealthy(),is(false));
    }


    @Test
    public void shouldNotAlertWhenGaugeIsLow() {
        GaugeTooLargeHealthCheck check = new GaugeTooLargeHealthCheck<>("Foo", fixedValueGauge,2000);
        assertThat(check.execute().isHealthy(), is(true));
    }

}
