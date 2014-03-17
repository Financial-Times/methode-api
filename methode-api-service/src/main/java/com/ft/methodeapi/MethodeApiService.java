package com.ft.methodeapi;

import com.ft.api.util.transactionid.TransactionIdFilter;
import com.ft.methodeapi.atc.AirTrafficController;
import com.ft.methodeapi.atc.LastKnownLocation;
import com.ft.methodeapi.atc.WhereIsMethodeResource;
import com.ft.methodeapi.service.methode.connection.DefaultMethodeObjectFactory;
import com.ft.methodeapi.service.methode.MethodeContentRetrievalHealthCheck;

import com.ft.methodeapi.service.methode.monitoring.GaugeTooLargeHealthCheck;
import com.ft.methodeapi.service.methode.monitoring.ThreadsByClassGauge;
import com.ft.ws.lib.swagger.SwaggerBundle;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ft.api.jaxrs.errors.RuntimeExceptionMapper;
import com.ft.api.util.VersionResource;
import com.ft.api.util.buildinfo.BuildInfoResource;
import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import com.ft.methodeapi.service.methode.MethodePingHealthCheck;
import com.ft.methodeapi.service.http.EomFileResource;
import com.ft.methodeapi.service.http.GetAssetTypeResource;
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
		bootstrap.addBundle(new SwaggerBundle());
    }

    @Override
    public void run(MethodeApiConfiguration configuration, Environment environment) {
    	LOGGER.info("running with configuration: {}", configuration);
        final MethodeConnectionConfiguration methodeConnectionConfiguration = configuration.getMethodeConnectionConfiguration();

        final MethodeObjectFactory methodeObjectFactory = createMethodeObjectFactory(methodeConnectionConfiguration,environment);
        final MethodeObjectFactory testMethodeObjectFactory = createMethodeObjectFactory(configuration.getMethodeTestConnectionConfiguration(),environment);

        final MethodeFileRepository methodeContentRepository = new MethodeFileRepository(methodeObjectFactory, testMethodeObjectFactory);

        environment.addResource(new EomFileResource(methodeContentRepository));
        environment.addResource(new VersionResource(MethodeApiService.class));
        environment.addResource(new BuildInfoResource());
        environment.addResource(new GetAssetTypeResource(methodeContentRepository));

        ThreadsByClassGauge jacorbThreadGauge = new ThreadsByClassGauge(org.jacorb.util.threadpool.ConsumerTie.class);
        Metrics.newGauge(jacorbThreadGauge.getMetricName(),jacorbThreadGauge);
        environment.addHealthCheck(new GaugeTooLargeHealthCheck<>("Jacorb Threads",jacorbThreadGauge,900));

        final LastKnownLocation location = new LastKnownLocation(
                new AirTrafficController(configuration.getAtc()),
                environment.managedScheduledExecutorService("atc-%d",1)
        );
        environment.addHealthCheck(new MethodePingHealthCheck(location, methodeObjectFactory, configuration.getMaxPingMillis()));
        environment.addHealthCheck(new MethodePingHealthCheck(location, testMethodeObjectFactory, configuration.getMaxPingMillis()));
        environment.addResource(new WhereIsMethodeResource(location));

        environment.addHealthCheck(new MethodeContentRetrievalHealthCheck(location, methodeContentRepository));

        environment.addProvider(new RuntimeExceptionMapper());
		environment.addFilter(new TransactionIdFilter(), "/eom-file/*");
        environment.addFilter(new TransactionIdFilter(), "/asset-type/*");


    }

    private MethodeObjectFactory createMethodeObjectFactory(MethodeConnectionConfiguration methodeConnectionConfiguration,Environment environment) {
        MethodeObjectFactory result = DefaultMethodeObjectFactory.builder()
                    .withHost(methodeConnectionConfiguration.getMethodeHostName())
                    .withPort(methodeConnectionConfiguration.getMethodePort())
                    .withUsername(methodeConnectionConfiguration.getMethodeUserName())
                    .withPassword(methodeConnectionConfiguration.getMethodePassword())
					.withConnectionTimeout(methodeConnectionConfiguration.getConnectTimeout())
                    .withOrbClass(methodeConnectionConfiguration.getOrbClass())
                    .withOrbSingletonClass(methodeConnectionConfiguration.getOrbSingletonClass())
                    .withPooling(methodeConnectionConfiguration.getPool())
                    .withWorkerThreadPool(environment.managedScheduledExecutorService("MOF-worker-%d",2))
                    .build();

        if(result instanceof Managed) {
            environment.manage((Managed) result);
        }

        return result;

    }
}
