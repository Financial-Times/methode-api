package com.ft.methodeapi.service.methode;

import com.codahale.metrics.Gauge;

/**
 * <p>Wraps a gauge in order to force a known class name into the stacktrace for the test execution thread.</p>
 *
 * <p>This is a purely artificial device (and is also a misleading example) of a class that may appear in a stack
 * trace for a thread. A better example is {@link org.jacorb.util.threadpool.ConsumerTie}
 * which is the outermost class in the call-stack of suspected leaked threads in Jacorb 2.2.4.</p>
 *
 * @author Simon.Gibbs
 */
public class GaugeWrapper<T> {


    Gauge<T> theGauge;

    GaugeWrapper(Gauge<T> theGauge) {
        this.theGauge = theGauge;
    }

    public T queryGauge() {
        return theGauge.getValue();
    }

}
