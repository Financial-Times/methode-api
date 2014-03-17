package com.ft.methodeapi.service.methode;

import com.ft.methodeapi.atc.LastKnownLocation;
import com.yammer.metrics.core.HealthCheck;

public class MethodeContentRetrievalHealthCheck extends HealthCheck {

    private final MethodeFileRepository methodeContentRepository;
    private final LastKnownLocation location;

    public MethodeContentRetrievalHealthCheck(LastKnownLocation location, MethodeFileRepository methodeContentRepository) {
        super(String.format("methode content retrieval [%s]", methodeContentRepository.getClientRepositoryInfo()));
        this.methodeContentRepository = methodeContentRepository;
        this.location = location;
    }

    @Override
    protected Result check() {

        if(!location.lastReport().isAmIActive()) {
            return Result.healthy(LastKnownLocation.IS_PASSIVE_MSG);
        }

        methodeContentRepository.findFileByUuid("this can be anything we just care that we can perform the search");
        return Result.healthy("can search for content");
    }
}
