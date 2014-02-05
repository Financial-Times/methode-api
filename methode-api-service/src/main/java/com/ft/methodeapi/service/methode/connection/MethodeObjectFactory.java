package com.ft.methodeapi.service.methode.connection;

import EOM.FileSystemAdmin;
import EOM.Repository;
import EOM.Session;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;

/**
 * Allows access to Methode objects of various kinds. Access is guaranteed to be
 * safe if, and only if, the client calls the corresponding "maybeCloseXXX" method
 * for every "createXXX" methode that it calls. The suggested way to do so is
 * by using templates.
 *
 * @author Simon.Gibbs
 * @see com.ft.methodeapi.service.methode.templates.MethodeSessionOperationTemplate
 * @see com.ft.methodeapi.service.methode.templates.MethodeRepositoryOperationTemplate
 * @see com.ft.methodeapi.service.methode.templates.MethodeFileSystemAdminOperationTemplate
 */
public interface MethodeObjectFactory{

    FileSystemAdmin createFileSystemAdmin(Session session);

    Session createSession(Repository repository);

    NamingContextExt createNamingService(ORB orb);

    void maybeCloseNamingService(NamingContextExt namingService);

    Repository createRepository(NamingContextExt namingService);

    ORB createOrb();

    void maybeCloseFileSystemAdmin(FileSystemAdmin fileSystemAdmin);

    void maybeCloseSession(Session session);

    void maybeCloseOrb(ORB orb);

    void maybeCloseRepository(Repository repository);

    String getDescription();

}
