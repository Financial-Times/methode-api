package com.ft.methodeapi.service.methode.templates;

import EOM.FileSystemAdmin;
import EOM.Session;
import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;

/**
 * MethodeSessionFileOperationTemplate
 *
 * @author Simon.Gibbs
 */
public class MethodeSessionFileOperationTemplate<T> {

    private final MethodeObjectFactory methodeObjectFactory;

    public MethodeSessionFileOperationTemplate(MethodeObjectFactory client) {
        this.methodeObjectFactory = client;
    }

    public T doOperation(final SessionFileOperationCallback<T> fileSystemAdminCallback){
        final MethodeSessionOperationTemplate.SessionCallback<T> callback = new MethodeSessionOperationTemplate.SessionCallback<T>() {

            @Override
            public T doOperation(Session session) {
                FileSystemAdmin fileSystemAdmin = null;
                try {
                    fileSystemAdmin = methodeObjectFactory.createFileSystemAdmin(session);
                    return fileSystemAdminCallback.doOperation(fileSystemAdmin, session);

                } finally {
                    methodeObjectFactory.maybeCloseFileSystemAdmin(fileSystemAdmin);
                }
            }
        };

        MethodeSessionOperationTemplate<T> template = new MethodeSessionOperationTemplate<>(methodeObjectFactory);
        return template.doOperation(callback);
    }

    public interface SessionFileOperationCallback<T> {
        T doOperation(FileSystemAdmin fileSystemAdmin, Session session);


    }

}
