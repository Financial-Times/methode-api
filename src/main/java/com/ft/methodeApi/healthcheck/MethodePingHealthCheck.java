package com.ft.methodeApi.healthcheck;

import com.ft.methodeApi.connectivity.EomRepositoryFactory;
import com.yammer.metrics.core.HealthCheck;

public class MethodePingHealthCheck extends HealthCheck {

    private final String methodeHostName;
    private final int methodePort;

    public MethodePingHealthCheck(String methodeHostName, int methodePort) {
        super("methode ping");

        this.methodeHostName = methodeHostName;
        this.methodePort = methodePort;
    }

    @Override
    protected Result check() throws Exception {
        try (EomRepositoryFactory eomRepositoryFactory = new EomRepositoryFactory(methodeHostName, methodePort)) {
            eomRepositoryFactory.createRepository().ping();
            return Result.healthy();
        }
    }
}
