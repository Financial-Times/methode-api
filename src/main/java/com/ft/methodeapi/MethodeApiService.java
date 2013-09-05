package com.ft.methodeapi;

import com.ft.methodeapi.healthcheck.MethodeLoginHealthcheck;
import com.ft.methodeapi.healthcheck.MethodePingHealthCheck;
import com.ft.methodeapi.service.ContentResource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class MethodeApiService extends Service<MethodeApiConfiguation> {

    public static void main(String[] args) throws Exception {
        System.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        System.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        new MethodeApiService().run(args);
    }

    @Override
    public void initialize(Bootstrap<MethodeApiConfiguation> bootstrap) {
    }

    @Override
    public void run(MethodeApiConfiguation configuration, Environment environment) throws Exception {
        final MethodeConnectionConfiguration methodeConnectionConfiguration = configuration.getMethodeConnectionConfiguration();

        environment.addResource(new ContentResource(methodeConnectionConfiguration.getMethodeHostName(),
                methodeConnectionConfiguration.getMethodePort(), methodeConnectionConfiguration.getMethodeUserName(),
                methodeConnectionConfiguration.getMethodePassword()));
        environment.addHealthCheck(new MethodePingHealthCheck(methodeConnectionConfiguration.getMethodeHostName(),
                methodeConnectionConfiguration.getMethodePort()));
        environment.addHealthCheck(new MethodeLoginHealthcheck(methodeConnectionConfiguration.getMethodeHostName(),
                methodeConnectionConfiguration.getMethodePort(), methodeConnectionConfiguration.getMethodeUserName(),
                methodeConnectionConfiguration.getMethodePassword()));
    }
}
