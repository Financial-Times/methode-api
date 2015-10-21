package com.ft.methodeapi.service.methode;

import com.codahale.metrics.health.HealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodeContentRetrievalHealthCheck extends HealthCheck {

    private final MethodeFileRepository methodeContentRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeContentRetrievalHealthCheck.class);

    public MethodeContentRetrievalHealthCheck(MethodeFileRepository methodeContentRepository) {
        this.methodeContentRepository = methodeContentRepository;
    }

    @Override
    protected Result check() {
        try {
            methodeContentRepository.findFileByUuid("this can be anything we just care that we can perform the search");
            return Result.healthy("can search for content");
        } catch (Exception e) {
            final String message = "cannot search for content";
            LOGGER.warn(message,e); // use WARN to prevent duplicate alerts
            return Result.unhealthy(message,e);
        }
    }
}
