package com.ft.methodeapi.service.methode.connection;

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
    int poolSize;
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
        if(poolSize>0) {
            factory = new PoolingMethodeObjectFactory(factory, executorService, poolSize);
        }

        return factory;
    }

    public MethodeObjectFactoryBuilder withPooling(int poolSize) {
        this.poolSize = poolSize;
        return this;
    }
}
