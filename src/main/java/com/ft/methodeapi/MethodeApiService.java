package com.ft.methodeapi;

import com.ft.methodeapi.healthcheck.MethodeContentSearchHealthcheck;
import com.ft.methodeapi.healthcheck.MethodePingHealthCheck;
import com.ft.methodeapi.service.ContentResource;
import com.ft.methodeapi.service.MethodeContentRepository;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class MethodeApiService extends Service<MethodeApiConfiguation> {

    public static void main(String[] args) throws Exception {
        new MethodeApiService().run(args);
    }

    @Override
    public void initialize(Bootstrap<MethodeApiConfiguation> bootstrap) {
    }

    @Override
    public void run(MethodeApiConfiguation configuration, Environment environment) {
        final MethodeConnectionConfiguration methodeConnectionConfiguration = configuration.getMethodeConnectionConfiguration();

        final MethodeContentRepository methodeContentRepository = MethodeContentRepository.builder()
                .withHost(methodeConnectionConfiguration.getMethodeHostName())
                .withPort(methodeConnectionConfiguration.getMethodePort())
                .withUsername(methodeConnectionConfiguration.getMethodeUserName())
                .withPassword(methodeConnectionConfiguration.getMethodePassword())
                .withHost(methodeConnectionConfiguration.getOrbSingletonClass())
                .withOrbClass(methodeConnectionConfiguration.getOrbClass())
                .withOrbClass(methodeConnectionConfiguration.getOrbSingletonClass())
                .build();

        environment.addResource(new ContentResource(methodeContentRepository));
        environment.addHealthCheck(new MethodePingHealthCheck(methodeContentRepository, configuration.getMaxPingMillis()));
        environment.addHealthCheck(new MethodeContentSearchHealthcheck(methodeContentRepository));
    }
}
