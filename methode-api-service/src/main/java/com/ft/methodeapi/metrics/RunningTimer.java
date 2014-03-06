package com.ft.methodeapi.metrics;

import com.google.common.base.Optional;

/**
 * Represents an instance of a timer in use in it's running state.
 *
 * @author Simon.Gibbs
 */
public interface RunningTimer {

    /**
     * Stops the timer, fixes its value, and executes any associated reporting logic.
     */
    public void stop();

    /**
     * The value of the time, or {@link com.google.common.base.Optional#absent() Optional#absent()} if absent for any reason at all.
     * Garaunted to have an immutable value after {@link #stop()} is called.
     * @return an optional result, or interim result in milliseconds
     */
    public Optional<Long> value();
}
