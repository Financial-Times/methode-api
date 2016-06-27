package com.ft.methodeapi;

import com.ft.api.util.transactionid.TransactionIdFilter;
import com.ft.methodeapi.service.methode.MethodeContentRetrievalHealthCheck;
import com.ft.methodeapi.service.methode.connection.MethodeObjectFactoryBuilder;
import com.ft.methodeapi.service.methode.monitoring.GaugeRangeHealthCheck;
import com.ft.methodeapi.service.methode.monitoring.ThreadsByClassGauge;

import io.dropwizard.lifecycle.Managed;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ft.api.jaxrs.errors.RuntimeExceptionMapper;
import com.ft.api.util.VersionResource;
import com.ft.api.util.buildinfo.BuildInfoResource;
import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import com.ft.methodeapi.service.methode.MethodePingHealthCheck;
import com.ft.methodeapi.service.http.EomFileResource;
import com.ft.methodeapi.service.http.GetAssetTypeResource;
import com.ft.methodeapi.service.methode.HealthcheckParameters;
import com.ft.methodeapi.service.methode.MethodeFileRepository;
import com.ft.methodeapi.service.methode.MethodeConnectionConfiguration;
import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedHealthCheckBundle;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import javax.servlet.DispatcherType;

public class MethodeApiApplication extends Application<MethodeApiConfiguration> {
    public static final String METHODE_API_PANIC_GUIDE = "https://sites.google.com/a/ft.com/dynamic-publishing-team/home/methode-api";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodeApiApplication.class);
	
	public static void main(String[] args) throws Exception {
        new MethodeApiApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<MethodeApiConfiguration> bootstrap) {
        bootstrap.addBundle(new AdvancedHealthCheckBundle());
    }

    @Override
    public void run(MethodeApiConfiguration configuration, Environment environment) throws Exception{
    	LOGGER.info("running with configuration: {}", configuration);

        final MethodeObjectFactory methodeObjectFactory = createMethodeObjectFactory("main", configuration.getCredentialsPath(),configuration.getMethodeConnectionConfiguration(),environment);
        final MethodeObjectFactory testMethodeObjectFactory = createMethodeObjectFactory("test-rw", configuration.getTestCredentialsPath(), configuration.getMethodeTestConnectionConfiguration(),environment);
        final MethodeFileRepository methodeContentRepository = new MethodeFileRepository.Builder()
            .withClientMethodeObjectFactory(methodeObjectFactory)
            .withTestClientMethodeObjectFactory(testMethodeObjectFactory)
            .build();

        environment.jersey().register(new EomFileResource(methodeContentRepository));
        environment.jersey().register(new VersionResource(MethodeApiApplication.class));
        environment.jersey().register(new BuildInfoResource());
        environment.jersey().register(new GetAssetTypeResource(methodeContentRepository));

        int poolingConnectionCount = countPoolingConnections(methodeObjectFactory,testMethodeObjectFactory);
        if(poolingConnectionCount>0) {
            ThreadsByClassGauge stormPotAllocatorThreadGauge = new ThreadsByClassGauge("stormpot.QAllocThread");
            String name = MetricRegistry.name(MethodeApiApplication.class, "StormpotAllocatorThreads");
            environment.metrics().register(name, stormPotAllocatorThreadGauge);
            environment.healthChecks().register("Stormpot Allocator Threads",
                    new GaugeRangeHealthCheck<>(
                            new HealthcheckParameters("Stormpot Allocator Threads", 3,
                                    "Methode API may not be able to assign server resources to fulfil requests.",
                                    "Allocator thread pool is outside of expected range.",
                                    METHODE_API_PANIC_GUIDE),
                            stormPotAllocatorThreadGauge,poolingConnectionCount,poolingConnectionCount));
        }

        ThreadsByClassGauge jacorbThreadGauge = new ThreadsByClassGauge(org.jacorb.util.threadpool.ConsumerTie.class);
        String jacorbMetricName = MetricRegistry.name(MethodeApiApplication.class, "JacorbThreads");
        environment.metrics().register(jacorbMetricName, jacorbThreadGauge);
        environment.healthChecks().register("Jacorb Threads",
                new GaugeRangeHealthCheck<>(
                        new HealthcheckParameters("Jacorb Threads", 3,
                                "Methode API may not be able to assign server resources to fulfil requests.",
                                "Jacorb thread pool is outside of expected range.",
                                METHODE_API_PANIC_GUIDE),
                        jacorbThreadGauge, 1, 900));

        String readHealthcheckName = "Methode Ping (read connection)";
        environment.healthChecks().register(readHealthcheckName,
                new MethodePingHealthCheck(
                        new HealthcheckParameters(readHealthcheckName, 3,
                                "Methode API cannot read information from the Methode servers.",
                                String.format("Methode API cannot ping the Methode repository at [%s].", methodeObjectFactory.getDescription()),
                                METHODE_API_PANIC_GUIDE), 
                        methodeObjectFactory, configuration.getMethodeConnectionConfiguration().getMaxPingMillis()));
        
        String testWriteHealthcheckName = "Methode Ping (test write connection)";
        environment.healthChecks().register(testWriteHealthcheckName,
                new MethodePingHealthCheck(
                        new HealthcheckParameters(testWriteHealthcheckName, 3,
                                "Methode API cannot write test information to the Methode server.",
                                String.format("Methode API cannot ping the Methode repository at [%s].", testMethodeObjectFactory.getDescription()),
                                METHODE_API_PANIC_GUIDE),
                                testMethodeObjectFactory, configuration.getMethodeTestConnectionConfiguration().getMaxPingMillis()));
        
        String contentHealthcheckName = "Methode content retrieval";
        environment.healthChecks().register(contentHealthcheckName,
                new MethodeContentRetrievalHealthCheck(
                        new HealthcheckParameters(contentHealthcheckName, 3,
                                "Methode API cannot read information from the Methode server.",
                                String.format("Methode API cannot make a findFileByUuid() call to the Methode repository at [%s].", methodeContentRepository.getClientRepositoryInfo()),
                                METHODE_API_PANIC_GUIDE), 
                        methodeContentRepository));

        environment.jersey().register(new RuntimeExceptionMapper());
		environment.servlets().addFilter("Transaction ID Filter", new TransactionIdFilter()).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/eom-file/*");
        environment.servlets().addFilter("Transaction ID Filter", new TransactionIdFilter()).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/asset-type/*");
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

    private MethodeObjectFactory createMethodeObjectFactory(String name, String credentialsPath, MethodeConnectionConfiguration methodeConnectionConfiguration,Environment environment) throws IOException{

        Properties credentials = new Properties();
        credentials.load(new FileReader(new File(credentialsPath)));

        String userName = credentials.getProperty("methode.api.userName");
        String password = credentials.getProperty("methode.api.password");

        MethodeObjectFactory result = MethodeObjectFactoryBuilder.named(name)
                    .withHost(methodeConnectionConfiguration.getMethodeHostName())
                    .withPort(methodeConnectionConfiguration.getMethodePort())
                    .withUsername(userName)
                    .withPassword(password)
					.withConnectionTimeout(methodeConnectionConfiguration.getConnectTimeout())
                    .withOrbClass(methodeConnectionConfiguration.getOrbClass())
                    .withOrbSingletonClass(methodeConnectionConfiguration.getOrbSingletonClass())
                    .withPooling(methodeConnectionConfiguration.getPool())
                    .withWorkerThreadPool(environment.lifecycle().executorService("MOF-worker-%d").maxThreads(2).build())
                    .withMetricRegistry(environment.metrics())
                    .build();

        if(result instanceof Managed) {
            environment.lifecycle().manage((Managed) result);
        }

        List<HealthCheck> checks = result.createHealthChecks();
        
        int i = 0;
        for(HealthCheck check : checks) {
            String healthCheckName =
                    (check instanceof AdvancedHealthCheck) ? ((AdvancedHealthCheck)check).getName()
                                                           : "MethodeObjectFactoryHealthCheck-" + (++i);
                    
            environment.healthChecks().register(healthCheckName, check);
        }

        return result;
    }
}
