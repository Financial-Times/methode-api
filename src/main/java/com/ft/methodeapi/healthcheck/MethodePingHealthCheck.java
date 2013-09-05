package com.ft.methodeapi.healthcheck;

import java.util.concurrent.TimeUnit;

import com.ft.methodeapi.service.MethodeContentRepository;
import com.yammer.metrics.core.HealthCheck;

public class MethodePingHealthCheck extends HealthCheck {

    private final MethodeContentRepository methodeContentRepository;

    public MethodePingHealthCheck(MethodeContentRepository methodeContentRepository) {
        super("methode ping");

        this.methodeContentRepository = methodeContentRepository;
    }

    @Override
    protected Result check() throws Exception {
        long startNanos = System.nanoTime();
        methodeContentRepository.ping();
        long durationNanos = System.nanoTime() - startNanos;
        long durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos);
        if (durationMillis > 1) {
            return Result.unhealthy("ping took too long %d ms", durationMillis);
        }
        return Result.healthy();
    }
}
