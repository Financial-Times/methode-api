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
import com.ft.methodeapi.repository.ContentRepository;
import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodeContentRepository implements ContentRepository, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeContentRepository.class);

    private final ORB orb;
    private Session session;

    private boolean isClosed = false;

    public MethodeContentRepository(String hostname, int port, String username, String password) {

        String[] orbInits = {"-ORBInitRef", String.format("NS=corbaloc:iiop:%s:%d/NameService", hostname, port)};
        this.orb = ORB.init(orbInits, new Properties());
        try {
            NamingContextExt namingService = NamingContextExtHelper.narrow(orb.resolve_initial_references("NS"));
            Repository eomRepo = RepositoryHelper.narrow(namingService.resolve_str("EOM/Repositories/cms"));
            this.session = eomRepo.login(username, password, "", null);
        } catch (CannotProceed | InvalidLogin | NotFound
                | org.omg.CORBA.ORBPackage.InvalidName
                | org.omg.CosNaming.NamingContextPackage.InvalidName
                | RepositoryError e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Timed
    public Optional<Content> findContentByUuid(String uuid) {
        if (isClosed) {
            throw new IllegalStateException("already closed");
        }

        FileSystemAdmin fileSystemAdmin = null;
        try {
            fileSystemAdmin = EOM.FileSystemAdminHelper.narrow(session.resolve_initial_references("FileSystemAdmin"));
        } catch (ObjectNotFound | RepositoryError | PermissionDenied e) {
            throw new RuntimeException(e);
        }

        String uri = "eom:/uuids/" + uuid;

        FileSystemObject fso;
        try {
            fso = fileSystemAdmin.get_object_with_uri(uri);
            if (isContent(fso.get_type_name())) {
                EOM.File eomFile = EOM.FileHelper.narrow(fso);
                Content content = new Content(eomFile.read_all());
                return Optional.of(content);
            } else {
                return Optional.absent();
            }
        } catch (InvalidURI invalidURI) {
            return Optional.absent();
        } catch (RepositoryError | PermissionDenied e) {
            throw new RuntimeException(e);
        }

    }

    private boolean isContent(String type) {
        return "EOM::Story".equals(type) || "EOM::CompoundStory".equals(type);
    }

    public void close() {
        isClosed = true;
        try {
            maybeCloseSession();
        } finally {
            maybeCloseOrb();
        }
    }

    private void maybeCloseOrb() {
        if (orb != null) {
            orb.destroy();
        }
    }

    private void maybeCloseSession() {
        if (session != null) {
            try {
                session.destroy();
            } catch (PermissionDenied | ObjectLocked | RepositoryError e) {
                LOGGER.warn("failed to destroy EOM.Session", e);
            } finally {
                session = null;
            }
        }
    }
}
