package com.ft.methodeapi.service.methode;

import EOM.Repository;

import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import com.ft.methodeapi.service.methode.templates.MethodeRepositoryOperationTemplate;
import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;
import com.ft.timer.FTTimer;
import com.ft.timer.RunningTimer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodePingHealthCheck
        extends AdvancedHealthCheck {

    private static final FTTimer pingTime = FTTimer.newTimer(MethodePingHealthCheck.class,"ping");

    private final MethodeObjectFactory methodeObjectFactory;
    private final long maxPingMillis;

    private final Logger LOGGER = LoggerFactory.getLogger(MethodePingHealthCheck.class);
    
    private final HealthcheckParameters params;
    
    public MethodePingHealthCheck(HealthcheckParameters params, MethodeObjectFactory objectFactory, long maxPingMillis) {
        super(params.getName());
        this.params = params;
        this.methodeObjectFactory = objectFactory;
        this.maxPingMillis = maxPingMillis;
    }

    @Override
    protected AdvancedResult checkAdvanced() throws Exception {

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

        // should never happen
        if(!timer.value().isPresent()) {
            String message = "Failed to time the ping";
            LOGGER.warn(message); // use WARN to prevent duplicate alerts
            Result.unhealthy(message);
        }

        long durationMillis = timer.value().get();

        AdvancedResult result;
        if (durationMillis > maxPingMillis) {
            String message = String.format("ping took too long %dms, max allowed is %dms", durationMillis, maxPingMillis);
            LOGGER.warn(message); // use WARN to prevent duplicate alerts
            result = AdvancedResult.error(this, message);
        } else {
            result = AdvancedResult.healthy(String.format("ping took %dms, within max allowed %dms", durationMillis, maxPingMillis));
        }
        return result;
    }

    @Override
    protected String businessImpact() {
        return params.getBusinessImpact();
    }

    @Override
    protected String panicGuideUrl() {
        return params.getPanicGuideUrl();
    }

    @Override
    protected int severity() {
        return params.getSeverity();
    }

    @Override
    protected String technicalSummary() {
        return params.getTechnicalSummary();
    }
}
