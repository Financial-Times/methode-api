package com.ft.methodeapi.service.methode;

import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodeContentRetrievalHealthCheck
        extends AdvancedHealthCheck {

    private final MethodeFileRepository methodeContentRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeContentRetrievalHealthCheck.class);
    
    private final HealthcheckParameters params;
    
    public MethodeContentRetrievalHealthCheck(HealthcheckParameters params, MethodeFileRepository methodeContentRepository) {
        super(params.getName());
        this.params = params;
        this.methodeContentRepository = methodeContentRepository;
    }

    @Override
    protected AdvancedResult checkAdvanced() throws Exception {
        try {
            methodeContentRepository.findFileByUuid("this can be anything we just care that we can perform the search");
            return AdvancedResult.healthy("can search for content");
        } catch (Exception e) {
            final String message = "cannot search for content";
            LOGGER.warn(message,e); // use WARN to prevent duplicate alerts
            return AdvancedResult.error(this, message,e);
        }
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
