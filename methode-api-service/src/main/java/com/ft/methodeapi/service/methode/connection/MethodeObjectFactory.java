package com.ft.methodeapi.service.methode.connection;

import EOM.Repository;
import EOM.Session;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;

/**
 * MethodeObjectFactory
 *
 * @author Simon.Gibbs
 */
public interface MethodeObjectFactory {
    Session createSession(Repository repository);

    NamingContextExt createNamingService(ORB orb);

    void maybeCloseNamingService(NamingContextExt namingService);

    Repository createRepository(NamingContextExt namingService);

    ORB createOrb();

    void maybeCloseSession(Session session);

    void maybeCloseOrb(ORB orb);

    void maybeCloseRepository(Repository repository);

    String getDescription();

}
