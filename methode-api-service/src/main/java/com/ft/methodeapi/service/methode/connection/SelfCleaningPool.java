package com.ft.methodeapi.service.methode.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stormpot.Completion;
import stormpot.LifecycledResizablePool;
import stormpot.PoolException;
import stormpot.Poolable;
import stormpot.Timeout;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>Removes bad Poolable's from the pool in the event the <code>pool.claim()</code> method begins
 * to throw exceptions. For systems with infrequent high-value transactions, this allows clients
 * to quickly move on from outages that cause the whole pool to become poisoned.</p>
 *
 * <p>Cleaning is performed by claiming and releasing Poolables such that any cached exceptions
 * are removed from Slots (e.g {@link stormpot.bpool.BSlot#poison})</p>
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
	private static final long DREDGE_PERIOD = 60;

	private final LifecycledResizablePool<T> implementation;
	private final ScheduledExecutorService executorService;
	private final Set<Class<? extends Throwable>> recoverableExceptions;

	private boolean dredging = false;

	private final Runnable dredgePool = new Runnable() {

		private final Timeout claimTimeout = new Timeout(500,TimeUnit.MILLISECONDS);

		@Override
		public void run() {
			for(int i =0; i<implementation.getTargetSize();i++) {
				T poolable = null;
				try {
					poolable = implementation.claim(claimTimeout);
				} catch (InterruptedException ie) {
					// never mind, we tried...
					LOGGER.debug("Interrupted while checking Poolable {}",i);
				} catch(Throwable t) {
					LOGGER.warn("Pool is still producing exceptions", t);
				} finally {
					if(poolable!=null) {
						poolable.release();
					}
				}
			}

			dredging = false;

		}
	};

	/**
	 * Adapts a {@link LifecycledResizablePool} to add self-cleaning behavior when expected exception
	 * types are
	 * @param implementation the adapted {@link LifecycledResizablePool} implementation
	 * @param recoverableExceptionTypes white listed exception types that will be logged and discarded
	 */
	@SafeVarargs
	public SelfCleaningPool(LifecycledResizablePool<T> implementation, Class<? extends Throwable>... recoverableExceptionTypes) {
		this.implementation = implementation;
		this.executorService = Executors.newSingleThreadScheduledExecutor();
		this.recoverableExceptions = new HashSet<>( recoverableExceptionTypes.length);
		for(Class<? extends Throwable> type : recoverableExceptionTypes) {
			recoverableExceptions.add(type);
		}
	}

	@Override
	public Completion shutdown() {
		executorService.shutdown();
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
			if(!dredging && expectedException(pe)) {
				dredging = true;
				executorService.schedule(dredgePool, DREDGE_PERIOD, TimeUnit.SECONDS);
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
