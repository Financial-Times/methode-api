package com.ft.methodeapi.service.methode.templates;

import EOM.Repository;
import EOM.Session;

import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;

/**
 * Encapsulates orchestration required to find, open and close the correct sequence of Methode objects to
 * obtain a Session and perform operations on it. The operation to be performed is passed as a callback.
 *
 * Thread safety is not assured.
 *
 * @author Simon.Gibbs
 */
public class MethodeSessionOperationTemplate<T> {

    private final MethodeObjectFactory client;
    private final Class<?> timerClass;
    private final String timerName;

    /** Constructor for untimed operations.
     *  @param client the MethodeObjectFactory
     */
    public MethodeSessionOperationTemplate(MethodeObjectFactory client) {
        this(client, null, null);
    }
    
    /** Constructor for operations whose time will be recorded in DropWizard metrics.
     *  @param client the MethodeObjectFactory
     *  @param timerClass the class against which metrics will be recorded
     *  @param timerName the timer name against which metrics will be recorded (partitioned by Methode IP address)
     */
    public MethodeSessionOperationTemplate(MethodeObjectFactory client, Class<?> timerClass, String timerName) {
        this.client = client;
        this.timerClass = timerClass;
        this.timerName = timerName;
    }
    
    public T doOperation(final SessionCallback<T> callback) {

        final MethodeRepositoryOperationTemplate.RepositoryCallback<T> repositoryCallback = new MethodeRepositoryOperationTemplate.RepositoryCallback<T>() {
            @Override
            public T doOperation(Repository repository) {

                Session session = null;
                try {
                    session = client.createSession(repository);

                    return callback.doOperation(session);

                } finally {
                    client.maybeCloseSession(session);
                }

            }
        };

        final MethodeRepositoryOperationTemplate<T> template = new MethodeRepositoryOperationTemplate<>(client, timerClass, timerName);

        return template.doOperation(repositoryCallback);
    }



    public static interface SessionCallback<T> extends MethodeCallback<T,Session> {

        public T doOperation(Session session);

    }
}
