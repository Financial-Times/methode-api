package com.ft.methodeapi.service.methode;

import EOM.FileSystemAdmin;
import EOM.FileSystemAdminHelper;
import EOM.FileSystemObject;
import EOM.FileSystemObjectsHelper;
import EOM.Folder;
import EOM.FolderHelper;
import EOM.InvalidURI;
import EOM.ObjectLocked;
import EOM.ObjectNotFound;
import EOM.ObjectTypeHelper;
import EOM.PermissionDenied;
import EOM.Repository;
import EOM.RepositoryError;
import EOM.Session;
import EOM.Utils;
import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.service.methode.templates.MethodeRepositoryOperationTemplate;
import com.ft.methodeapi.service.methode.templates.MethodeSessionOperationTemplate;
import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodeFileRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeFileRepository.class);

    private final MethodeObjectFactory client;


    public MethodeFileRepository(MethodeObjectFactory client) {
        this.client = client;
    }

    @Timed
    public void ping() {
        new MethodeRepositoryOperationTemplate<>(client).doOperation(new MethodeRepositoryOperationTemplate.RepositoryCallback<Void>() {
            @Override
            public Void doOperation(Repository repository) {
                repository.ping();
                return null;
            }
        });
    }

    @Timed
    public Optional<EomFile> findFileByUuid(final String uuid) {

        final MethodeSessionOperationTemplate<Optional<EomFile>> template = new MethodeSessionOperationTemplate<>(client);

        MethodeSessionOperationTemplate.SessionCallback<Optional<EomFile>> callback;

        callback=new MethodeSessionOperationTemplate.SessionCallback<Optional<EomFile>>() {
            @Override
            public Optional<EomFile> doOperation(Session session, Repository repository) {
                final FileSystemAdmin fileSystemAdmin;
                try {
                    fileSystemAdmin = EOM.FileSystemAdminHelper.narrow(session.resolve_initial_references("FileSystemAdmin"));
                } catch (ObjectNotFound | RepositoryError | PermissionDenied e) {
                    throw new MethodeException(e);
                }

                String uri = "eom:/uuids/" + uuid;

                FileSystemObject fso;
                Optional<EomFile> foundContent;
                try {
                    fso = fileSystemAdmin.get_object_with_uri(uri);

                    EOM.File eomFile = EOM.FileHelper.narrow(fso);

                    final String typeName = eomFile.get_type_name();
                    final byte[] bytes = eomFile.read_all();
                    final String attributes = eomFile.get_attributes();
                    EomFile content = new EomFile(uuid, typeName, bytes, attributes);
                    foundContent = Optional.of(content);

                    eomFile._release();
                    fileSystemAdmin._release();

                } catch (InvalidURI invalidURI) {
                    return Optional.absent();
                } catch (RepositoryError | PermissionDenied e) {
                    throw new MethodeException(e);
                }
                return foundContent;
            }
        };

       return template.doOperation(callback);
    }

    private static final String FOLDER = "/FT Website Production/Z_Test/dyn_pub_test";

    public EomFile createNewFile(final String filename, final EomFile eomFile) {
        final MethodeSessionOperationTemplate<EomFile> template = new MethodeSessionOperationTemplate<>(client);
        final EomFile createdEomFile = template.doOperation(new CreateFileCallback(FOLDER, filename, eomFile));
        return createdEomFile;
    }

    public void deleteFileByUuid(final String uuid) {
        final MethodeSessionOperationTemplate<Void> template = new MethodeSessionOperationTemplate<>(client);
        template.doOperation(new MethodeSessionOperationTemplate.SessionCallback<Void>() {
            @Override
            public Void doOperation(Session session, Repository repository) {
                final FileSystemAdmin fileSystemAdmin;
                try {
                    fileSystemAdmin = FileSystemAdminHelper.narrow(session.resolve_initial_references("FileSystemAdmin"));
                } catch (ObjectNotFound | RepositoryError | PermissionDenied e) {
                    throw new MethodeException(e);
                }

                final String uri = "eom:/uuids/" + uuid;

                final FileSystemObject fso;
                try {
                    fso = fileSystemAdmin.get_object_with_uri(uri);
                    try {
                        EOM.File eomFile = EOM.FileHelper.narrow(fso);
                        eomFile.discard();
                    } finally {
                        fso._release();
                    }
                } catch (InvalidURI e) {
                    throw new NotFoundException(uuid);
                } catch (PermissionDenied | RepositoryError | ObjectLocked e) {
                    throw new MethodeException(e);
                } finally {
                    fileSystemAdmin._release();
                }

                return null;
            }
        });
    }


}
