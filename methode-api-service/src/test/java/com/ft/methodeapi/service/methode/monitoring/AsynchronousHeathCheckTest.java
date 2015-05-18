package com.ft.methodeapi.service.methode.monitoring;

import com.yammer.metrics.core.HealthCheck;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * AsynchronousHeathCheckTest
 *
 * @author Simon.Gibbs
 */
@RunWith(MockitoJUnitRunner.class)
public class AsynchronousHeathCheckTest {

    public static final String SYNTHETIC_FAILURE = "Synthetic failure";
    public static final String SYNTHETIC_PASS = "Synthetic pass";

    @Mock
    HealthCheck slowHealthCheck;

    @Mock
    HealthCheck fastFailingHealthCheck;

    @Mock
    HealthCheck fastPassingHealthCheck;

    @Before
    public void setup() {
        when(slowHealthCheck.getName()).thenReturn("Slow H/C");
        when(slowHealthCheck.execute()).then(new Answer<HealthCheck.Result>() {
            @Override
            public HealthCheck.Result answer(InvocationOnMock invocationOnMock) throws Throwable {
                Thread.sleep(4000);
                return HealthCheck.Result.healthy();
            }
        });

        when(fastFailingHealthCheck.getName()).thenReturn("Fast Failing H/C");
        when(fastFailingHealthCheck.execute()).then(new Answer<HealthCheck.Result>() {
            @Override
            public HealthCheck.Result answer(InvocationOnMock invocationOnMock) throws Throwable {
                return HealthCheck.Result.unhealthy(SYNTHETIC_FAILURE);
            }
        });

        when(fastPassingHealthCheck.getName()).thenReturn("Fast Passing H/C");
        when(fastPassingHealthCheck.execute()).then(new Answer<HealthCheck.Result>() {
            @Override
            public HealthCheck.Result answer(InvocationOnMock invocationOnMock) throws Throwable {
                return HealthCheck.Result.healthy(SYNTHETIC_PASS);
            }
        });

    }

    @Test
    public void shouldBeHeathlyBeforeFirstCheck() {
        AsynchronousHeathCheck sit = new AsynchronousHeathCheck(slowHealthCheck, Executors.newSingleThreadScheduledExecutor(),4L, TimeUnit.SECONDS);
        assertTrue(sit.execute().isHealthy());
    }

    @Test
    public void shouldBeUnheathlyIfTheCheckHasNotBeenDone() {

        // the slow check will take 4s, set up an expectation of half a second, and check well after double that period

        AsynchronousHeathCheck sit = new AsynchronousHeathCheck(slowHealthCheck, Executors.newSingleThreadScheduledExecutor(),500L, TimeUnit.MILLISECONDS);
        pauseFor(1500);
        assertFalse(sit.execute().isHealthy());
    }

    @Test
    public void shouldFailIfTargetDoes() {

        AsynchronousHeathCheck sit = new AsynchronousHeathCheck(fastFailingHealthCheck, Executors.newSingleThreadScheduledExecutor(),250L, TimeUnit.MILLISECONDS);
        pauseFor(500);

        HealthCheck.Result result = sit.execute();

        assertFalse(result.isHealthy());
        assertThat(result.getMessage(),is(SYNTHETIC_FAILURE));

    }

    @Test
    public void shouldPassIfTargetDoes() {

        AsynchronousHeathCheck sit = new AsynchronousHeathCheck(fastPassingHealthCheck, Executors.newSingleThreadScheduledExecutor(),250L, TimeUnit.MILLISECONDS);
        pauseFor(500);

        HealthCheck.Result result = sit.execute();

        assertTrue(result.isHealthy());
        assertThat(result.getMessage(),is(SYNTHETIC_PASS));

    }

    private void pauseFor(int millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

}
