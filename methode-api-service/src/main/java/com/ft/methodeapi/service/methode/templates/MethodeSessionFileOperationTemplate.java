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
    private final Class<?> timerClass;
    private final String timerName;
    
    /** Constructor for untimed operations.
     *  @param client the MethodeObjectFactory
     */
    public MethodeSessionFileOperationTemplate(MethodeObjectFactory client) {
        this(client, null, null);
    }

    /** Constructor for operations whose time will be recorded in DropWizard metrics.
     *  @param client the MethodeObjectFactory
     *  @param timerClass the class against which metrics will be recorded
     *  @param timerName the timer name against which metrics will be recorded (partitioned by Methode IP address)
     */
    public MethodeSessionFileOperationTemplate(MethodeObjectFactory client, Class<?> timerClass, String timerName) {
        this.methodeObjectFactory = client;
        this.timerClass = timerClass;
        this.timerName = timerName;
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

        MethodeSessionOperationTemplate<T> template = new MethodeSessionOperationTemplate<>(methodeObjectFactory, timerClass, timerName);
        return template.doOperation(callback);
    }

    public interface SessionFileOperationCallback<T> {
        T doOperation(FileSystemAdmin fileSystemAdmin, Session session);


    }

}
