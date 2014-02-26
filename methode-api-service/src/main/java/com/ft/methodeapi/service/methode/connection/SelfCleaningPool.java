package com.ft.methodeapi.service.methode.connection;

import com.ft.methodeapi.metrics.FTTimer;
import com.ft.methodeapi.metrics.RunningTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stormpot.Completion;
import stormpot.LifecycledResizablePool;
import stormpot.PoolException;
import stormpot.Poolable;
import stormpot.Timeout;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>Removes bad Poolable's from the pool in the event the <code>pool.claim()</code> method begins
 * to throw exceptions. For systems with infrequent high-value transactions, this allows clients
 * to quickly move on from outages that cause the whole pool to become poisoned.</p>
 *
 * <p>Cleaning is performed by claiming and releasing Poolables such that any cached exceptions
 * are removed from Slots (see {@link stormpot.bpool.BSlot#poison})</p>
 *
 * <p>Excess load and log noise are avoided by scheduling an asynchronous "dredge" not more than every
 * {@link SelfCleaningPool#DREDGE_PERIOD DREDGE_PERIOD} seconds, and by white listing recoverable exception
 * types.</p>
 *
 * <p>Exceptions, once cleared, are logged and discarded.</p>
 *
 * @author Simon Gibbs
 */
public class SelfCleaningPool<T extends Poolable> implements LifecycledResizablePool<T> {


	private static final Logger LOGGER = LoggerFactory.getLogger(SelfCleaningPool.class);
	private static final long DREDGE_PERIOD = 30;
    private static FTTimer dredgeTimer  = FTTimer.newTimer(SelfCleaningPool.class,"pool-dredging");

    private final LifecycledResizablePool<T> implementation;
	private final ScheduledExecutorService executorService;
	private final Set<Class<? extends Throwable>> recoverableExceptions;
    private final long dredgeDelay;

    private final AtomicBoolean dredging = new AtomicBoolean(false);

	private final Runnable dredgePool = new Runnable() {

		private final Timeout claimTimeout = new Timeout(500,TimeUnit.MILLISECONDS);

		@Override
		public void run() {
            RunningTimer timer = dredgeTimer.start();
			for(int i =0; i<implementation.getTargetSize();i++) {
				T poolable = null;
				try {
					poolable = implementation.claim(claimTimeout);
				} catch (InterruptedException ie) {
					// Okay, let's stop then
					LOGGER.debug("Interrupted while checking Poolable {}",i);
                    dredging.set(false); // allow dredging to resume
                    break;
				} catch(Throwable t) {
					LOGGER.warn("Pool is still producing exceptions", t);
				} finally {
					if(poolable!=null) {
						poolable.release();
					}
				}
			}
			dredging.set(false);
            timer.stop();
		}
	};

	/**
	 * <p>Adapts a {@link LifecycledResizablePool} to add self-cleaning behavior when expected exception
	 * types are thrown from <code>implementation.claim(to)</code></p>
     *
     * <p>In order to avoid the inheritance of an {@link org.slf4j.MDC MDC} or other state from client threads,
     * the worker thread used for clean up tasks will be spawned immediately.</p>
     *
     * @see ch.qos.logback.classic.util.LogbackMDCAdapter#copyOnInheritThreadLocal LogbackMDCAdapter
     * @see org.slf4j.helpers.BasicMDCAdapter#inheritableThreadLocal BasicMDCAdapter
     *
	 * @param implementation the adapted {@link LifecycledResizablePool} implementation
     * @param dredgeDelay number of seconds to wait before dredging out bad connections
	 * @param recoverableExceptionTypes white listed exception types that will be logged and discarded
	 */
	@SafeVarargs
	public SelfCleaningPool(LifecycledResizablePool<T> implementation, ScheduledExecutorService executorService, long dredgeDelay, Class<? extends Throwable>... recoverableExceptionTypes) {
		this.implementation = implementation;
		this.executorService = executorService;

        /* Submit a task to force the executorService to spawn it's threads now.
         * See JavaDoc for rationale and links.
         */
        this.executorService.execute(new Runnable() {
            @Override
            public void run() {
                // just some helpful logging, an empty Runnable would have done.
                LOGGER.debug("Pool cleanup thread initialised");
            }
        });

		this.recoverableExceptions = new HashSet<>( recoverableExceptionTypes.length);
		for(Class<? extends Throwable> type : recoverableExceptionTypes) {
			recoverableExceptions.add(type);
		}
        this.dredgeDelay = dredgeDelay;
	}

    @SafeVarargs
    public SelfCleaningPool(LifecycledResizablePool<T> implementation, ScheduledExecutorService executorService, Class<? extends Throwable>... recoverableExceptionTypes) {
        this(implementation, executorService, DREDGE_PERIOD, recoverableExceptionTypes);
    }

	@Override
	public Completion shutdown() {
        dredging.set(true); // stop further dredging
        LOGGER.info("Shut down requested");
        return implementation.shutdown();
	}

	@Override
	public void setTargetSize(int i) {
		implementation.setTargetSize(i);
	}

	@Override
	public int getTargetSize() {
		return implementation.getTargetSize();
	}

	@Override
	public T claim(Timeout timeout) throws PoolException, InterruptedException {
		try {
			return implementation.claim(timeout);
		} catch (PoolException pe) {
			if(!dredging.get() && expectedException(pe)) {
				dredging.set(true); // prevent additional dredging until this run completes
				executorService.schedule(dredgePool, dredgeDelay, TimeUnit.SECONDS);
                LOGGER.info("Dredging scheduled",pe);
			}
			throw pe;
		}
	}

	private boolean expectedException(PoolException pe) {
		Set<Throwable> causes = findPossibleRootCauses(pe);
		for(Throwable possibleCause : causes) {
			Class<? extends Throwable> causeClass = possibleCause.getClass();
			if(recoverableExceptions.contains(causeClass)) {
		 		return true;
			}
		}
		return false;
	}

	private Set<Throwable> findPossibleRootCauses(Throwable proximateCause) {
		Set<Throwable> causes = new HashSet<>(2);
		causes.add(proximateCause);
		causes.add(proximateCause.getCause());

		return causes;
	}
}
