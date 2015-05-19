package com.ft.methodeapi.service.methode.connection;

import EOM.FileSystemAdmin;
import EOM.Repository;
import EOM.Session;
import com.yammer.metrics.core.HealthCheck;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;

import java.util.List;

/**
 * <p>Allows access to Methode objects of various kinds. Access is guaranteed to be
 * safe if, and only if, the client calls the corresponding "maybeCloseXXX" method
 * for every "createXXX" methode that it calls. The suggested way to do so is
 * by using templates.</p>
 *
 * <p>maybeCloseXXX methods are mandated to swallow exceptions. createXXX methods
 * are mandated to throw a {@link com.ft.methodeapi.service.methode.MethodeException MethodeException}
 * in place of checked exceptions.</p>
 *
 * @author Simon.Gibbs
 * @see com.ft.methodeapi.service.methode.templates.MethodeSessionOperationTemplate
 * @see com.ft.methodeapi.service.methode.templates.MethodeRepositoryOperationTemplate
 * @see com.ft.methodeapi.service.methode.templates.MethodeFileSystemAdminOperationTemplate
 */
public interface MethodeObjectFactory {

    FileSystemAdmin createFileSystemAdmin(Session session);

    Session createSession(Repository repository);

    NamingContextExt createNamingService(ORB orb);

    Repository createRepository(NamingContextExt namingService);

    ORB createOrb();

    List<HealthCheck> createHealthChecks();

    boolean isPooling();

    /**
     * Attempt to end the object lifecycle, while suppressing any and all exceptions
     * @param fileSystemAdmin the object
     */
    void maybeCloseFileSystemAdmin(FileSystemAdmin fileSystemAdmin);

    /**
     * Attempt to end the object lifecycle, while suppressing any and all exceptions
     * @param session the object
     */
    void maybeCloseSession(Session session);

    /**
     * Attempt to end the object lifecycle, while suppressing any and all exceptions
     * @param namingService the object
     */
    void maybeCloseNamingService(NamingContextExt namingService);

    /**
     * Attempt to end the object lifecycle, while suppressing any and all exceptions
     * @param repository the object
     */
    void maybeCloseRepository(Repository repository);

    /**
     * Attempt to end the object lifecycle, while suppressing any and all exceptions
     * @param orb the object
     */
    void maybeCloseOrb(ORB orb);

    String getName();

    String getDescription();
    
    /** Refresh the object factory state and establish where future Methode calls will be made.
     *  @return an identifier for the Methode location
     */
    String refreshMethodeLocation();

}
