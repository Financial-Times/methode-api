package com.ft.methodeapi.service;

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
import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodeContentRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeContentRepository.class);

    private final String hostname;
    private final int port;
    private final String username;
    private final String password;

    public MethodeContentRepository(String hostname, int port, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public MethodeContentRepository(Builder builder) {
        this(builder.host, builder.port, builder.username, builder.password);
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
    public Optional<Content> findContentByUuid(String uuid) {
        ORB orb = createOrb();
        try {
            return findContentByUuidWithOrb(uuid, orb);
        } finally {
            maybeCloseOrb(orb);
        }
    }

    private Optional<Content> findContentByUuidWithOrb(String uuid, ORB orb) {
        Session session = createSession(orb);
        try {
            return findContentByUuidWithSession(uuid, session);
        } finally {
            maybeCloseSession(session);
        }
    }

    private Optional<Content> findContentByUuidWithSession(String uuid, Session session) {
        FileSystemAdmin fileSystemAdmin;
        try {
            fileSystemAdmin = EOM.FileSystemAdminHelper.narrow(session.resolve_initial_references("FileSystemAdmin"));
        } catch (ObjectNotFound | RepositoryError | PermissionDenied e) {
            throw new RuntimeException(e);
        }

        String uri = "eom:/uuids/" + uuid;

        FileSystemObject fso;
        Optional<Content> foundContent;
        try {
            fso = fileSystemAdmin.get_object_with_uri(uri);
            if (isContent(fso.get_type_name())) {
                EOM.File eomFile = EOM.FileHelper.narrow(fso);
                Content content = new Content(eomFile.read_all());
                foundContent = Optional.of(content);
            } else {
                foundContent = Optional.absent();
            }
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
        NamingContextExt namingService = null;
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
        return ORB.init(orbInits, new Properties());
    }

    private boolean isContent(String type) {
        return "EOM::Story".equals(type) || "EOM::CompoundStory".equals(type);
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

        public MethodeContentRepository build() {
            return new MethodeContentRepository(this);
        }
    }
}
