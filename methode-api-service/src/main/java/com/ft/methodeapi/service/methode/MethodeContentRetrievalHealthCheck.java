package com.ft.methodeapi.service.methode;

import com.yammer.metrics.core.HealthCheck;

public class MethodeContentRetrievalHealthCheck extends HealthCheck {

    private final MethodeFileRepository methodeContentRepository;

    public MethodeContentRetrievalHealthCheck(MethodeFileRepository methodeContentRepository) {
        super(String.format("methode content retrieval [%s]", methodeContentRepository.getClientRepositoryInfo()));
        this.methodeContentRepository = methodeContentRepository;
    }

    @Override
    protected Result check() {
        methodeContentRepository.findFileByUuid("this can be anything we just care that we can perform the search");
        return Result.healthy("can search for content");
    }
}
