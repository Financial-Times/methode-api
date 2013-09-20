package com.ft.methodeapi.service.methode;

import java.util.concurrent.TimeUnit;

import com.yammer.metrics.core.HealthCheck;

public class MethodePingHealthCheck extends HealthCheck {

    private final MethodeFileRepository methodeContentRepository;
    private final long maxPingMillis;

    public MethodePingHealthCheck(MethodeFileRepository methodeContentRepository, long maxPingMillis) {
        super("methode ping");

        this.methodeContentRepository = methodeContentRepository;
        this.maxPingMillis = maxPingMillis;
    }

    @Override
    protected Result check() throws Exception {
        long startNanos = System.nanoTime();
        methodeContentRepository.ping();
        long durationNanos = System.nanoTime() - startNanos;
        long durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos);
        Result result;
        if (durationMillis > maxPingMillis) {
            result = Result.unhealthy("ping took too long %dms, max allowed is %dms", durationMillis, maxPingMillis);
        } else {
            result = Result.healthy("ping took %dms, within max allowed %dms", durationMillis, maxPingMillis);
        }
        return result;
    }
}
