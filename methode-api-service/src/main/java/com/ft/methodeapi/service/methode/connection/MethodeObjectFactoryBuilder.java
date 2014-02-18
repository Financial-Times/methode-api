package com.ft.methodeapi.service.methode.connection;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.util.concurrent.ScheduledExecutorService;

/**
* MethodeObjectFactoryBuilder
*
* @author Simon.Gibbs
*/
public class MethodeObjectFactoryBuilder {

    String username;
    String password;
    String host;
    int port;
    int connectionTimeout;
    String orbClass;
    String orbSingletonClass;
    Optional<PoolConfiguration> pool;
    ScheduledExecutorService executorService;

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

    public MethodeObjectFactoryBuilder withWorkerThreadPool(ScheduledExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    public MethodeObjectFactory build() {

        MethodeObjectFactory factory = new DefaultMethodeObjectFactory(this);
        if(pool.isPresent() && pool.get().getSize()>0) {

            Preconditions.checkState(executorService!=null,"Connection pooling requires a thread pool");

            factory = new PoolingMethodeObjectFactory(factory, executorService, pool.get());
        }

        return factory;
    }

    public MethodeObjectFactoryBuilder withPooling(Optional<PoolConfiguration> pool) {
        this.pool = pool;
        return this;
    }
}
