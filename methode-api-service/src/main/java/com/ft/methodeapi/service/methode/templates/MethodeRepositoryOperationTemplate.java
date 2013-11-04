package com.ft.methodeapi.service.methode.templates;

import EOM.Repository;
import com.ft.methodeapi.service.methode.MethodeObjectFactory;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;

/**
 * Encapsulates orchestration required to find, open and close the correct sequence of Methode objects to
 * obtain a Repository and perform operations on it. The operation to be performed is passed as a callback.
 *
 * Thread safety is not assured.
 *
 * @author Simon.Gibbs
 */
public class MethodeRepositoryOperationTemplate<T> {

    private final MethodeObjectFactory client;

    public MethodeRepositoryOperationTemplate(MethodeObjectFactory client) {
        this.client = client;
    }

    public T doOperation(RepositoryCallback<? extends T> callback) {

        ORB orb = client.createOrb();
        try {
            return doOperationWithOrb(orb, callback);

        } finally {
            client.maybeCloseOrb(orb);
        }
    }

    private T doOperationWithOrb(ORB orb, RepositoryCallback<? extends T> callback) {
        NamingContextExt namingService = client.createNamingService(orb);
        try {
            return doOperationWithNamingService(namingService, callback);
        } finally {
            client.maybeCloseNamingService(namingService);
        }
    }

    private T doOperationWithNamingService(NamingContextExt namingService, RepositoryCallback<? extends T> callback) {
        Repository repository = client.createRepository(namingService);
        try {

            return callback.doOperation(repository);

        } finally {
            client.maybeCloseRepository(repository);
        }
    }

    public static interface RepositoryCallback<T> {

        public T doOperation(Repository repository);

    }




}
