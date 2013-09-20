package com.ft.methodeapi.service.methode;

import com.yammer.metrics.core.HealthCheck;

public class MethodeContentSearchHealthcheck extends HealthCheck {

    private final MethodeFileRepository methodeContentRepository;

    public MethodeContentSearchHealthcheck(MethodeFileRepository methodeContentRepository) {
        super("methode login");
        this.methodeContentRepository = methodeContentRepository;
    }

    @Override
    protected Result check() {
        methodeContentRepository.findFileByUuid("this can be anything we just care that we can perform the search");
        return Result.healthy("can search for content");
    }
}
