package com.ft.methodeapi.service.methode.connection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.yammer.dropwizard.util.Duration;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * PoolConfiguration
 *
 * @author Simon.Gibbs
 */
public class PoolConfiguration {

    private final int size;
    private final Duration timeout;
    private final Duration methodeStaleConnectionTimeout;

    public PoolConfiguration(@JsonProperty("size") Optional<Integer> size, @JsonProperty("timeout") Optional<Duration> timeout,
    		@JsonProperty("methodeStaleConnectionTimeout") Optional<Duration> methodeStaleConnectionTimeout) {
        this.size = size.or(0); // implies no pooling by default
        this.timeout = timeout.or(Duration.milliseconds(1900)); // based on 99.9 percentile in TEST
        this.methodeStaleConnectionTimeout = methodeStaleConnectionTimeout.or(Duration.seconds(60 * 30)); 
    }

    /**
     * Gets the number of objects to pool
     * @return a positive int, or zero if disabled
     */
    @Min(0) @NotNull
    public int getSize() {
        return size;
    }

    /**
     * Gets a timeout for the <code>pool.claim(timeout)</code> call. This is separate from the
     * methode connection timeout because that timeout can be employed asynchronously. Client
     * requests fail if this time is exceeded by the pool.
     * @return the length of time to wait for an allocated connection
     * @see stormpot.Pool#claim(stormpot.Timeout)
     */
    public Duration getTimeout() {
        return timeout;
    }

	/**
	 * Gets a timeout for when to consider that a methode connection is stale - if it's not been used 
	 * for this amount of time, don't even do any checks, just deallocate and reallocate
	 * 
	 * @return the timeout for when to consider a methode connection is stale
	 */
    public Duration getMethodeStaleConnectionTimeout() {
		return methodeStaleConnectionTimeout;
	}

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("size",size)
                .add("timeout",timeout)
                .add("methodeStaleConnectionTimeout", methodeStaleConnectionTimeout)
                .toString();
    }

}
