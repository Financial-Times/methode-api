package com.ft.methodeapi;

import com.ft.api.util.transactionid.TransactionIdFilter;
import com.ft.dropwizard.killswitchtask.KillSwitchTask;
import com.ft.methodeapi.service.methode.MethodeContentRetrievalHealthCheck;

import com.ft.methodeapi.service.methode.connection.MethodeObjectFactoryBuilder;
import com.ft.methodeapi.service.methode.monitoring.GaugeRangeHealthCheck;
import com.ft.methodeapi.service.methode.monitoring.ThreadsByClassGauge;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.HealthCheck;
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

import java.util.List;

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

        final MethodeObjectFactory methodeObjectFactory = createMethodeObjectFactory("main", configuration.getMethodeConnectionConfiguration(),environment);
        final MethodeObjectFactory testMethodeObjectFactory = createMethodeObjectFactory("test-rw", configuration.getMethodeTestConnectionConfiguration(),environment);

        final MethodeFileRepository methodeContentRepository = new MethodeFileRepository(methodeObjectFactory, testMethodeObjectFactory);

        environment.addResource(new EomFileResource(methodeContentRepository));
        environment.addResource(new VersionResource(MethodeApiService.class));
        environment.addResource(new BuildInfoResource());
        environment.addResource(new GetAssetTypeResource(methodeContentRepository));

        int poolingConnectionCount = countPoolingConnections(methodeObjectFactory,testMethodeObjectFactory);
        if(poolingConnectionCount>0) {
            ThreadsByClassGauge stormPotAllocatorThreadGauge = new ThreadsByClassGauge("stormpot.QAllocThread");
            Metrics.newGauge(stormPotAllocatorThreadGauge.getMetricName(),stormPotAllocatorThreadGauge);
            environment.addHealthCheck(new GaugeRangeHealthCheck<>("Stormpot Allocator Threads",stormPotAllocatorThreadGauge,poolingConnectionCount,poolingConnectionCount));
        }

        ThreadsByClassGauge jacorbThreadGauge = new ThreadsByClassGauge(org.jacorb.util.threadpool.ConsumerTie.class);
        Metrics.newGauge(jacorbThreadGauge.getMetricName(),jacorbThreadGauge);
        environment.addHealthCheck(new GaugeRangeHealthCheck<>("Jacorb Threads",jacorbThreadGauge,1,900));



        environment.addHealthCheck(new MethodePingHealthCheck(methodeObjectFactory, configuration.getMethodeConnectionConfiguration().getMaxPingMillis()));
        environment.addHealthCheck(new MethodePingHealthCheck(testMethodeObjectFactory, configuration.getMethodeTestConnectionConfiguration().getMaxPingMillis()));

        environment.addHealthCheck(new MethodeContentRetrievalHealthCheck(methodeContentRepository));

        environment.addProvider(new RuntimeExceptionMapper());
		environment.addFilter(new TransactionIdFilter(), "/eom-file/*");
        environment.addFilter(new TransactionIdFilter(), "/asset-type/*");

        environment.addTask(new KillSwitchTask());
    }

    private int countPoolingConnections(MethodeObjectFactory...  factories) {
        int result = 0;
        for(MethodeObjectFactory factory : factories) {
            if(factory.isPooling()) {
                result++;
            }
        }
        return result;
    }

    private MethodeObjectFactory createMethodeObjectFactory(String name, MethodeConnectionConfiguration methodeConnectionConfiguration,Environment environment) {
        MethodeObjectFactory result = MethodeObjectFactoryBuilder.named(name)
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

        List<HealthCheck> checks = result.createHealthChecks();

        for(HealthCheck check : checks) {
            environment.addHealthCheck(check);
        }

        return result;

    }
}
