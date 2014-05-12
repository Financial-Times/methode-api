package com.ft.methodeapi.service.methode.monitoring;

import com.google.common.base.Preconditions;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricName;

import java.util.Map;

/**
 * ThreadsByClassGauge
 *
 * @author Simon.Gibbs
 */
public class ThreadsByClassGauge extends Gauge<Integer> {

    private String drivingClassName;
    private MetricName metricName;

    public ThreadsByClassGauge(Class<?> drivingClass) {
        this(drivingClass.getCanonicalName());
    }

    public ThreadsByClassGauge(String drivingClassCanonicalClassName) {
        Preconditions.checkArgument(drivingClassCanonicalClassName!=null,"must have a canonical name");
        this.drivingClassName = drivingClassCanonicalClassName;
        metricName = new MetricName(this.getClass(),drivingClassName);
    }

    @Override
    public Integer value() {
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

    public MetricName getMetricName() {
        return metricName;
    }
}
