package com.ft.methodeapi.healthcheck;

import com.ft.methodeapi.service.MethodeContentRepository;
import com.yammer.metrics.core.HealthCheck;

public class MethodeContentSearchHealthcheck extends HealthCheck {

    private final MethodeContentRepository methodeContentRepository;

    public MethodeContentSearchHealthcheck(MethodeContentRepository methodeContentRepository) {
        super("methode login");
        this.methodeContentRepository = methodeContentRepository;
    }

    @Override
    protected Result check() {
        methodeContentRepository.findContentByUuid("this can be anything we just care that we can perform the search");
        return Result.healthy("can search for content");
    }
}
