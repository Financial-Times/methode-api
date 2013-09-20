package com.ft.methodeapi.service.methode;

import java.util.Properties;

import EOM.FileSystemAdmin;
import EOM.FileSystemObject;
import EOM.InvalidURI;
import EOM.ObjectLocked;
import EOM.ObjectNotFound;
import EOM.PermissionDenied;
import EOM.Repository;
import EOM.RepositoryError;
import EOM.RepositoryHelper;
import EOM.RepositoryPackage.InvalidLogin;
import EOM.Session;
import com.ft.methodeapi.model.EomFile;
import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodeFileRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeFileRepository.class);

    private final String hostname;
    private final int port;
    private final String username;
    private final String password;
    private final String orbClass;
    private final String orbSingletonClass;

    public MethodeFileRepository(String hostname, int port, String username, String password, String orbClass, String orbSingletonClass) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.orbClass = orbClass;
        this.orbSingletonClass = orbSingletonClass;
    }

    public MethodeFileRepository(Builder builder) {
        this(builder.host, builder.port, builder.username, builder.password, builder.orbClass, builder.orbSingletonClass);
    }

    @Timed
    public void ping() {
        ORB orb = createOrb();
        try {
            createRepository(orb).ping();
        } finally {
            maybeCloseOrb(orb);
        }
    }

    @Timed
    public Optional<EomFile> findFileByUuid(String uuid) {
        ORB orb = createOrb();
        try {
            return findFileByUuidWithOrb(uuid, orb);
        } finally {
            maybeCloseOrb(orb);
        }
    }

    private Optional<EomFile> findFileByUuidWithOrb(String uuid, ORB orb) {
        Session session = createSession(orb);
        try {
            return findFileByUuidWithSession(uuid, session);
        } finally {
            maybeCloseSession(session);
        }
    }

    private Optional<EomFile> findFileByUuidWithSession(String uuid, Session session) {
        FileSystemAdmin fileSystemAdmin;
        try {
            fileSystemAdmin = EOM.FileSystemAdminHelper.narrow(session.resolve_initial_references("FileSystemAdmin"));
        } catch (ObjectNotFound | RepositoryError | PermissionDenied e) {
            throw new RuntimeException(e);
        }

        String uri = "eom:/uuids/" + uuid;

        FileSystemObject fso;
        Optional<EomFile> foundContent;
        try {
            fso = fileSystemAdmin.get_object_with_uri(uri);

            EOM.File eomFile = EOM.FileHelper.narrow(fso);

            EomFile content = new EomFile(uuid, fso.get_type_name(), eomFile.read_all());
            foundContent = Optional.of(content);

        } catch (InvalidURI invalidURI) {
            return Optional.absent();
        } catch (RepositoryError | PermissionDenied e) {
            throw new RuntimeException(e);
        }
        return foundContent;
    }

    private Session createSession(ORB orb) {
        Session session;
        try {
            Repository repository = createRepository(orb);
            session = repository.login(username, password, "", null);
        } catch (InvalidLogin | RepositoryError e) {
            throw new RuntimeException(e);
        }
        return session;
    }

    private Repository createRepository(ORB orb) {
        NamingContextExt namingService;
        try {
            namingService = NamingContextExtHelper.narrow(orb.resolve_initial_references("NS"));
            return RepositoryHelper.narrow(namingService.resolve_str("EOM/Repositories/cms"));
        } catch (org.omg.CORBA.ORBPackage.InvalidName
                | org.omg.CosNaming.NamingContextPackage.InvalidName
                | CannotProceed | NotFound e) {
            throw new RuntimeException(e);
//        } finally {
//            // TODO is this safe? NO - so, do we need to clean up the naming service when we're finished?
//            if (namingService != null) {
//                try {
//                    namingService.destroy();
//                } catch (NotEmpty notEmpty) {
//                    throw new RuntimeException(notEmpty);
//                }
//            }
        }
    }

    private ORB createOrb() {
        String[] orbInits = {"-ORBInitRef", String.format("NS=corbaloc:iiop:%s:%d/NameService", hostname, port)};
        Properties properties = new Properties() {
            {
                setProperty("org.omg.CORBA.ORBClass", orbClass);
                setProperty("org.omg.CORBA.ORBSingletonClass", orbSingletonClass);
            }
        };
        return ORB.init(orbInits, properties);
    }

    private void maybeCloseSession(Session session) {
        if (session != null) {
            try {
                session.destroy();
            } catch (PermissionDenied | ObjectLocked | RepositoryError e) {
                LOGGER.warn("failed to destroy EOM.Session", e);
            }
        }
    }

    private void maybeCloseOrb(ORB orb) {
        if (orb != null) {
            orb.destroy();
        }
    }

    public static Builder builder() {
        return new Builder();
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

        public MethodeFileRepository build() {
            return new MethodeFileRepository(this);
        }
    }
}
