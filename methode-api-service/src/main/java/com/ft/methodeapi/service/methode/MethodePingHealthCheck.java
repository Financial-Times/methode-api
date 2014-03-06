package com.ft.methodeapi.service.methode;

import java.util.concurrent.TimeUnit;

import EOM.Repository;
import com.ft.methodeapi.metrics.FTTimer;
import com.ft.methodeapi.metrics.RunningTimer;
import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import com.ft.methodeapi.service.methode.templates.MethodeRepositoryOperationTemplate;
import com.yammer.metrics.core.HealthCheck;

public class MethodePingHealthCheck extends HealthCheck {

    private static final FTTimer pingTime = FTTimer.newTimer(MethodePingHealthCheck.class,"ping");

    private final MethodeObjectFactory methodeObjectFactory;
    private final long maxPingMillis;

    public MethodePingHealthCheck(MethodeObjectFactory objectFactory, long maxPingMillis) {
        super(String.format("methode ping [%s]", objectFactory.getDescription()));

        this.methodeObjectFactory = objectFactory;
        this.maxPingMillis = maxPingMillis;
    }

    @Override
    protected Result check() throws Exception {
        final RunningTimer timer = pingTime.start();
        new MethodeRepositoryOperationTemplate<>(methodeObjectFactory).doOperation(new MethodeRepositoryOperationTemplate.RepositoryCallback<Object>() {
            @Override
            public Object doOperation(Repository repository) {

                try {
                    repository.ping();
                    return null;
                } finally {
                    timer.stop();
                }
            }
        });

        if(!timer.value().isPresent()) {
            Result.unhealthy("Failed to time the ping"); // should never happen
        }

        long durationMillis = timer.value().get();

        Result result;
        if (durationMillis > maxPingMillis) {
            result = Result.unhealthy("ping took too long %dms, max allowed is %dms", durationMillis, maxPingMillis);
        } else {
            result = Result.healthy("ping took %dms, within max allowed %dms", durationMillis, maxPingMillis);
        }
        return result;
    }
}
