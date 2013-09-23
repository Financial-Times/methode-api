package com.ft.methodeapi.service.methode;

import EOM.ObjectLocked;
import EOM.PermissionDenied;
import EOM.Repository;
import EOM.RepositoryError;
import EOM.RepositoryHelper;
import EOM.RepositoryPackage.InvalidLogin;
import EOM.Session;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotEmpty;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Encapsulates logic to create and destroy Methode objects and holds the connection and credential details.
 *
 * @author Simon.Gibbs
 */
public class MethodeObjectFactory {

    private final String hostname;
    private final int port;
    private final String username;
    private final String password;
    private final String orbClass;
    private final String orbSingletonClass;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeObjectFactory.class);

    public MethodeObjectFactory(String hostname, int port, String username, String password, String orbClass, String orbSingletonClass) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.orbClass = orbClass;
        this.orbSingletonClass = orbSingletonClass;
    }

    public MethodeObjectFactory(Builder builder) {
        this(builder.host, builder.port, builder.username, builder.password, builder.orbClass, builder.orbSingletonClass);
    }

    public Session createSession(Repository repository) {
        Session session;
        try {
            session = repository.login(username, password, "", null);
        } catch (InvalidLogin | RepositoryError e) {
            throw new MethodeException(e);
        }
        return session;
    }

    public NamingContextExt createNamingService(ORB orb) {
        try {
            return NamingContextExtHelper.narrow(orb.resolve_initial_references("NS"));
        } catch (InvalidName invalidName) {
            throw new MethodeException(invalidName);
        }
    }

    public void maybeCloseNamingService(NamingContextExt namingService) {
        if (namingService != null) {
            //try {
                //namingService.destroy();
                namingService._release();
            //} catch (NotEmpty notEmpty) {
            //    throw new MethodeException(notEmpty);
            //}
        }
    }

    public Repository createRepository(NamingContextExt namingService) {
        try {
            return RepositoryHelper.narrow(namingService.resolve_str("EOM/Repositories/cms"));
        } catch (org.omg.CosNaming.NamingContextPackage.InvalidName
                | CannotProceed | NotFound e) {
            throw new MethodeException(e);
        }
    }

    public ORB createOrb() {
        String[] orbInits = {"-ORBInitRef", String.format("NS=corbaloc:iiop:%s:%d/NameService", hostname, port)};
        Properties properties = new Properties() {
            {
                setProperty("org.omg.CORBA.ORBClass", orbClass);
                setProperty("org.omg.CORBA.ORBSingletonClass", orbSingletonClass);
            }
        };
        return ORB.init(orbInits, properties);
    }

    public void maybeCloseSession(Session session) {
        if (session != null) {
            try {
                session.destroy();
                session._release();
            } catch (PermissionDenied | ObjectLocked | RepositoryError e) {
                LOGGER.warn("failed to destroy EOM.Session", e);
            }
        }
    }

    public void maybeCloseOrb(ORB orb) {
        if (orb != null) {
            orb.destroy();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public void maybeCloseRepository(Repository repository) {
        if(repository!=null) {
            repository._release();
        }
    }

    public static class Builder {

        private String username;
        private String password;
        private String host;
        private int port;
        private String orbClass;
        private String orbSingletonClass;

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withOrbClass(String orbClass) {
            this.orbClass = orbClass;
            return this;
        }

        public Builder withOrbSingletonClass(String orbSingletonClass) {
            this.orbSingletonClass = orbSingletonClass;
            return this;
        }

        public MethodeObjectFactory build() {
            return new MethodeObjectFactory(this);
        }
    }


}
