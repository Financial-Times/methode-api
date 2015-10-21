package com.ft.methodeapi.service.methode.monitoring;

import com.google.common.base.Preconditions;
import com.codahale.metrics.Gauge;

import java.util.Map;

/**
 * ThreadsByClassGauge
 *
 * @author Simon.Gibbs
 */
public class ThreadsByClassGauge implements Gauge<Integer> {

    private String drivingClassName;

    public ThreadsByClassGauge(Class<?> drivingClass) {
        this(drivingClass.getCanonicalName());
    }

    public ThreadsByClassGauge(String drivingClassCanonicalClassName) {
        Preconditions.checkArgument(drivingClassCanonicalClassName!=null,"must have a canonical name");
        this.drivingClassName = drivingClassCanonicalClassName;
    }

    @Override
    public Integer getValue() {
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        int value = 0;

        for(StackTraceElement[] stack : threads.values()) {
            for(StackTraceElement entry : stack) {
                String stackEntryClass = entry.getClassName();

                if(stackEntryClass.startsWith(drivingClassName)) {
                    value++;
                    break;
                }
            }
        }
        return value;
    }
}
