package com.ft.methodeapi.service.methode.connection;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.util.concurrent.ExecutorService;

/**
* MethodeObjectFactoryBuilder
*
* @author Simon.Gibbs
*/
public class MethodeObjectFactoryBuilder {

    String name;
    String username;
    String password;
    String host;
    int port;
    int connectionTimeout;
    String orbClass;
    String orbSingletonClass;
    Optional<PoolConfiguration> pool = Optional.absent();
    ExecutorService executorService;
    MetricRegistry metricRegistry;
    
    public static MethodeObjectFactoryBuilder named(String name) {
        return (new MethodeObjectFactoryBuilder()).withName(name);
    }

    public MethodeObjectFactoryBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public MethodeObjectFactoryBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public MethodeObjectFactoryBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public MethodeObjectFactoryBuilder withConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public MethodeObjectFactoryBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    public MethodeObjectFactoryBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    public MethodeObjectFactoryBuilder withOrbClass(String orbClass) {
        this.orbClass = orbClass;
        return this;
    }

    public MethodeObjectFactoryBuilder withOrbSingletonClass(String orbSingletonClass) {
        this.orbSingletonClass = orbSingletonClass;
        return this;
    }

    public MethodeObjectFactoryBuilder withWorkerThreadPool(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }
    
    public MethodeObjectFactoryBuilder withMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        return this;
    }
    
    public MethodeObjectFactory build() {

        MethodeObjectFactory factory = new DefaultMethodeObjectFactory(this);
        if(pool.isPresent() && pool.get().getSize()>0) {

            Preconditions.checkState(executorService!=null,"Connection pooling requires a thread pool");

            factory = new PoolingMethodeObjectFactory(factory, executorService, pool.get(), metricRegistry);
        }

        return factory;
    }

    public MethodeObjectFactoryBuilder withPooling(Optional<PoolConfiguration> pool) {
        this.pool = pool;
        return this;
    }
}
