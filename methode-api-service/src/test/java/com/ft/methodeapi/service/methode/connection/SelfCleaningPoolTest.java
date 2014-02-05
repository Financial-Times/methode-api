package com.ft.methodeapi.service.methode.connection;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import stormpot.LifecycledResizablePool;
import stormpot.PoolException;
import stormpot.Poolable;
import stormpot.Timeout;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SelfCleaningPoolTest
 *
 * @author Simon.Gibbs
 */
@RunWith(MockitoJUnitRunner.class)
public class SelfCleaningPoolTest {

    public static final int POOL_SIZE = 5;
    @Mock
    LifecycledResizablePool<Poolable> mockPool;

    Timeout exampleTimeout = new Timeout(10, TimeUnit.MILLISECONDS);
    RuntimeException exampleException = new RuntimeException("Mock exception");

    private SelfCleaningPool<Poolable> cleaningPool;

    private static ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(2);

    @Before
    public void setUpFailingPool() throws InterruptedException {

        // a self-cleaning pool, configured to work faster than normal
        cleaningPool = new SelfCleaningPool<>(mockPool,threadPool,1,RuntimeException.class);

        when(mockPool.getTargetSize()).thenReturn(POOL_SIZE);
        when(mockPool.claim(any(Timeout.class))).thenThrow(new PoolException("Example Poison",exampleException));
    }

    @AfterClass
    public void stopWorkerThreads() {
        threadPool.shutdown();
    }

    @Test
    public void shouldClaimTargetSizeConnectionsShortlyAfterARecoverableException() throws InterruptedException {

        try {
            cleaningPool.claim(exampleTimeout);
            fail("Expected exception");
        } catch (PoolException e) {
            assertEquals(e.getCause(),exampleException);
        }

        Thread.sleep(1100); // the SIT is configured to dredge quickly

        verify(mockPool,times(POOL_SIZE+1)).claim(any(Timeout.class));

    }

    @Test
    public void shouldNotClaimTargetSizeConnectionsShortlyAfterANonRecoverableException() throws InterruptedException {
    	// a self-cleaning pool, configured to work faster than normal
        cleaningPool = new SelfCleaningPool<>(mockPool,threadPool,1,IllegalArgumentException.class);
        try {
            cleaningPool.claim(exampleTimeout); // will throw RuntimeException
            fail("Expected exception");
        } catch (PoolException e) {
            assertEquals(e.getCause(),exampleException);
        }

        Thread.sleep(1100); // the SIT is configured to dredge quickly

        verify(mockPool,times(1)).claim(any(Timeout.class)); // just the one claim above

    }

    @Test
    public void shouldNotDredgeMoreThanOncePerDredgePeriod() throws InterruptedException {

        // Request very many claims
        int veryMany = POOL_SIZE*4;
        for(int i=0;i<veryMany;i++) {
            try {
                cleaningPool.claim(exampleTimeout);
                fail("Expected exception");
            } catch (PoolException e) {
                assertEquals(e.getCause(),exampleException);
            }
        }

        Thread.sleep(1100);

        // Expect the number we requested, and a number from the dredger equal to POOL_SIZE
        verify(mockPool,times(veryMany+POOL_SIZE)).claim(any(Timeout.class));

    }

}
