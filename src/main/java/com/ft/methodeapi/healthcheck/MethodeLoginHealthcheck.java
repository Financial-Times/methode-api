package com.ft.methodeapi.healthcheck;

import com.ft.methodeapi.connectivity.EomRepositoryFactory;
import com.ft.methodeapi.connectivity.EomSessionFactory;
import com.ft.methodeapi.connectivity.EomSessionWrapper;
import com.yammer.metrics.core.HealthCheck;

public class MethodeLoginHealthcheck extends HealthCheck {

    private final String methodeHostName;
    private final int methodePort;
    private final String methodeUserName;
    private final String methodePassword;

    public MethodeLoginHealthcheck(String methodeHostName, int methodePort, String methodeUserName, String methodePassword) {
        super("methode login");
        this.methodeHostName = methodeHostName;
        this.methodePort = methodePort;
        this.methodeUserName = methodeUserName;
        this.methodePassword = methodePassword;
    }

    @Override
    protected Result check() throws Exception {
        try (EomRepositoryFactory repositoryFactory = new EomRepositoryFactory(methodeHostName, methodePort);
             EomSessionWrapper sessionWrapper = new EomSessionFactory(methodeUserName, methodePassword, repositoryFactory.createRepository()).createSession()) {
            return Result.healthy("logged in to session as %s", sessionWrapper.getSession().get_owner());
        }
    }
}
