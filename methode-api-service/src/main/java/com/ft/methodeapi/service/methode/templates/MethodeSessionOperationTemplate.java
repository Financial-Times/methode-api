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

    public MethodeSessionOperationTemplate(MethodeObjectFactory client) {
        this.client = client;
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

        final MethodeRepositoryOperationTemplate<T> template = new MethodeRepositoryOperationTemplate<>(client);

        return template.doOperation(repositoryCallback);
    }



    public static interface SessionCallback<T> {

        public T doOperation(Session session);

    }
}
