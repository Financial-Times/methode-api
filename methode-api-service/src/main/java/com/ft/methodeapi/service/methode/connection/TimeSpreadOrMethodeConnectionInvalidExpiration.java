package com.ft.methodeapi.service.methode.connection;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stormpot.Expiration;
import stormpot.SlotInfo;

import java.util.concurrent.TimeUnit;

/**
 * Modified pseudo random expiration algorithm. The pseudo-random factor is derived
 * from object hash codes.
 * 
 * Also includes a 'quick fail' based on Methode connection here_i_am check, so if
 * a connection is no longer valid, it gets expired immediately. This check happens
 * AFTER the expiration check.
 *
 * @author Simon Gibbs
 * @author Chris Vest &lt;mr.chrisvest@gmail.com&gt;
 */
public class TimeSpreadOrMethodeConnectionInvalidExpiration implements Expiration<MethodeConnection> {

    private final long lowerBoundMillis;
    private final long upperBoundMillis;

    private final Histogram connectionAgeActual = Metrics.newHistogram(TimeSpreadOrMethodeConnectionInvalidExpiration.class,"actual","connection-age",true);
    private final Histogram connectionAgeExpected = Metrics.newHistogram(TimeSpreadOrMethodeConnectionInvalidExpiration.class,"expected","connection-age",true);

    private static Logger LOGGER = LoggerFactory.getLogger(TimeSpreadOrMethodeConnectionInvalidExpiration.class);


    /**
     * Construct a new Expiration that will invalidate objects that are older
     * than the given lower bound, before they get older than the upper bound,
     * in the given time unit. It will also invalidate objects with an invalid
     * Methode connection. 
     * <p>
     * If the <code>lowerBound</code> is less than 1, the <code>upperBound</code>
     * is less than the <code>lowerBound</code>, or the <code>unit</code> is
     * <code>null</code>, then an {@link IllegalArgumentException} will
     * be thrown.
     * <p/>
     * <p/>
     * <p>This method is an unaltered copy of the constructor from
     * TimeSpreadExpiration written by Chris vest as  part of StormPot 2.2.</p>
     *
     * @param lowerBound Poolables younger than this, in the given unit, are not
     *                   considered expired. This value must be at least 1.
     * @param upperBound Poolables older than this, in the given unit, are always
     *                   considered expired. This value must be greater than the
     *                   lowerBound.
     * @param unit       The {@link java.util.concurrent.TimeUnit} of the bounds values. Never
     *                   <code>null</code>.
     */
    public TimeSpreadOrMethodeConnectionInvalidExpiration(
            long lowerBound,
            long upperBound,
            TimeUnit unit) {
        if (lowerBound < 1) {
            throw new IllegalArgumentException("The lower bound cannot be less than 1.");
        }
        if (upperBound <= lowerBound) {
            throw new IllegalArgumentException("The upper bound must be greater than the lower bound.");
        }
        if (unit == null) {
            throw new IllegalArgumentException("The TimeUnit cannot be null.");
        }
        this.lowerBoundMillis = unit.toMillis(lowerBound);
        this.upperBoundMillis = unit.toMillis(upperBound);
    }

    /**
     * Will expire the Poolable if the upper bound has been exceeded 
     * OR if the Methode connection is no longer valid.
     * 
     * MAY expire it after the minimum time has expired and before the
     * the maximum time, based on a pseudorandom algorithm. This last is to 
     * ensure we don't get lots of Poolables expiring at the same time given,
     * for example, that they all get created on startup.
     * 
     * For the random algorithm, an expiry time is calculated by adding the modulo of
     * @{link Poolable#hashCode()} and the delta to the lower bound.
     */
    @Override
    public boolean hasExpired(SlotInfo<? extends MethodeConnection> info) {
        long maxDelta = upperBoundMillis - lowerBoundMillis;
        long expirationAge = lowerBoundMillis + Math.abs(info.hashCode() % maxDelta);

        long age = info.getAgeMillis();

        LOGGER.debug("Checking for expiration {}", info.getPoolable());

        if(age >= expirationAge) {
            connectionAgeActual.update(age);
            connectionAgeExpected.update(expirationAge);

            LOGGER.info("Expired connection after actual={}, expected={}",age,expirationAge);

            return true;
        }
        
        // now check the connection
        try {
            LOGGER.debug("Starting ping check {}", info.getPoolable());
        	info.getPoolable().getRepository().ping();
            if(info.getPoolable().getSession()._non_existent()) {
                LOGGER.warn("Session is gone");
                return true;
            }
        } catch (Exception e) {
        	LOGGER.info("Methode connection is no longer valid, expiring it", e);
            return true;
        }

        LOGGER.debug("Slot is current. {}", info.getPoolable());

        return false;

    }
}
