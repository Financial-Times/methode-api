package com.ft.methodeapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ft.api.jaxrs.errors.RuntimeExceptionMapper;
import com.ft.api.util.VersionResource;
import com.ft.api.util.buildinfo.BuildInfoResource;
import com.ft.methodeapi.service.methode.MethodeContentSearchHealthcheck;
import com.ft.methodeapi.service.methode.MethodeObjectFactory;
import com.ft.methodeapi.service.methode.MethodePingHealthCheck;
import com.ft.methodeapi.service.http.EomFileResource;
import com.ft.methodeapi.service.methode.MethodeFileRepository;
import com.ft.methodeapi.service.methode.MethodeConnectionConfiguration;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class MethodeApiService extends Service<MethodeApiConfiguration> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodeApiService.class);
	
	public static void main(String[] args) throws Exception {
        new MethodeApiService().run(args);
    }

    @Override
    public void initialize(Bootstrap<MethodeApiConfiguration> bootstrap) {
    }

    @Override
    public void run(MethodeApiConfiguration configuration, Environment environment) {
    	LOGGER.info("running with configuration: {}", configuration);
        final MethodeConnectionConfiguration methodeConnectionConfiguration = configuration.getMethodeConnectionConfiguration();

        final MethodeObjectFactory methodeObjectFactory = createMethodeObjectFactory(methodeConnectionConfiguration);
        final MethodeObjectFactory testMethodeObjectFactory = createMethodeObjectFactory(configuration.getMethodeTestConnectionConfiguration());

        final MethodeFileRepository methodeContentRepository = new MethodeFileRepository(methodeObjectFactory, testMethodeObjectFactory);

        environment.addResource(new EomFileResource(methodeContentRepository));
        environment.addResource(new VersionResource(MethodeApiService.class));
        environment.addResource(new BuildInfoResource());
        environment.addHealthCheck(new MethodePingHealthCheck(methodeContentRepository, configuration.getMaxPingMillis()));
        environment.addHealthCheck(new MethodeContentSearchHealthcheck(methodeContentRepository));
        environment.addProvider(new RuntimeExceptionMapper());
    }

    private MethodeObjectFactory createMethodeObjectFactory(MethodeConnectionConfiguration methodeConnectionConfiguration) {
        return MethodeObjectFactory.builder()
                    .withHost(methodeConnectionConfiguration.getMethodeHostName())
                    .withPort(methodeConnectionConfiguration.getMethodePort())
                    .withUsername(methodeConnectionConfiguration.getMethodeUserName())
                    .withPassword(methodeConnectionConfiguration.getMethodePassword())
                    .withOrbClass(methodeConnectionConfiguration.getOrbClass())
                    .withOrbSingletonClass(methodeConnectionConfiguration.getOrbSingletonClass())
                    .build();
    }
}
