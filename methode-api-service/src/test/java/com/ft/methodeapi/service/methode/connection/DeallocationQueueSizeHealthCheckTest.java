package com.ft.methodeapi.service.methode.connection;

import com.codahale.metrics.Gauge;
import com.ft.methodeapi.service.methode.HealthcheckParameters;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * DeallocationQueueSizeHealthCheckTest
 *
 * @author Simon.Gibbs
 */
@RunWith(MockitoJUnitRunner.class)
public class DeallocationQueueSizeHealthCheckTest {

    public static final int ALERT_THRESHOLD = 10;
    
    private static final HealthcheckParameters PARAMS = new HealthcheckParameters(
            "Test Health Check", 3, "Business Impact", "Technical Impact", "http://panic.example.org/"
            );
    
    @Mock
    private Gauge<Integer> mockGauge;

    @Test
    public void shouldFailGivenLongQueue() {

        when(mockGauge.getValue()).thenReturn(ALERT_THRESHOLD+1);

        DeallocationQueueSizeHealthCheck check = new DeallocationQueueSizeHealthCheck(PARAMS, mockGauge, ALERT_THRESHOLD);

        assertFalse(check.execute().isHealthy());

    }

    @Test
    public void shouldPassGivenQueueSizeUnderThreshold() {

        when(mockGauge.getValue()).thenReturn(ALERT_THRESHOLD-1);

        DeallocationQueueSizeHealthCheck check = new DeallocationQueueSizeHealthCheck(PARAMS, mockGauge, ALERT_THRESHOLD);

        assertTrue(check.execute().isHealthy());

    }


    @Test
    public void shouldFailGivenQueueSizeAtThreshold() {

        when(mockGauge.getValue()).thenReturn(ALERT_THRESHOLD);

        DeallocationQueueSizeHealthCheck check = new DeallocationQueueSizeHealthCheck(PARAMS, mockGauge, ALERT_THRESHOLD);

        assertFalse(check.execute().isHealthy());

    }

    @Test
    public void shouldPassEmptyQueue() {

        when(mockGauge.getValue()).thenReturn(0);

        DeallocationQueueSizeHealthCheck check = new DeallocationQueueSizeHealthCheck(PARAMS, mockGauge, ALERT_THRESHOLD);

        assertTrue(check.execute().isHealthy());

    }

}
