package com.ft.methodeapi.service.methode.connection;

import com.yammer.metrics.core.Gauge;
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

    @Mock
    private Gauge<Integer> mockGauge;

    @Test
    public void shouldFailGivenLongQueue() {

        when(mockGauge.value()).thenReturn(ALERT_THRESHOLD+1);

        DeallocationQueueSizeHealthCheck check = new DeallocationQueueSizeHealthCheck("test",mockGauge, ALERT_THRESHOLD);

        assertFalse(check.execute().isHealthy());

    }

    @Test
    public void shouldPassGivenQueueSizeUnderThreshold() {

        when(mockGauge.value()).thenReturn(ALERT_THRESHOLD-1);

        DeallocationQueueSizeHealthCheck check = new DeallocationQueueSizeHealthCheck("test",mockGauge, ALERT_THRESHOLD);

        assertTrue(check.execute().isHealthy());

    }


    @Test
    public void shouldFailGivenQueueSizeAtThreshold() {

        when(mockGauge.value()).thenReturn(ALERT_THRESHOLD);

        DeallocationQueueSizeHealthCheck check = new DeallocationQueueSizeHealthCheck("test",mockGauge, ALERT_THRESHOLD);

        assertFalse(check.execute().isHealthy());

    }

    @Test
    public void shouldPassEmptyQueue() {

        when(mockGauge.value()).thenReturn(0);

        DeallocationQueueSizeHealthCheck check = new DeallocationQueueSizeHealthCheck("test",mockGauge, ALERT_THRESHOLD);

        assertTrue(check.execute().isHealthy());

    }

}
