package com.ft.methodeapi.service.methode.connection;

import EOM.FileSystemAdmin;
import EOM.Repository;
import EOM.Session;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import stormpot.Poolable;
import stormpot.Slot;

/**
 * MethodeConnection
 *
 * @author Simon.Gibbs
 */
public class MethodeConnection implements Poolable {

    private final Slot slot;
    private final FileSystemAdmin fileSystemAdmin;
    private final Session session;
    private final Repository repository;
    private final NamingContextExt namingService;
    private final ORB orb;

    public MethodeConnection(Slot slot, ORB orb, NamingContextExt namingService, Repository repository, Session session, FileSystemAdmin fileSystemAdmin ) {
        this.slot = slot;
        this.fileSystemAdmin = fileSystemAdmin;
        this.session = session;
        this.repository = repository;
        this.namingService = namingService;
        this.orb = orb;
    }

    @Override
    public void release() {
        slot.release(this);
    }

    public FileSystemAdmin getFileSystemAdmin() {
        return fileSystemAdmin;
    }

    public Session getSession() {
        return session;
    }

    public Repository getRepository() {
        return repository;
    }

    public NamingContextExt getNamingService() {
        return namingService;
    }

    public ORB getOrb() {
        return orb;
    }

    @Override
    public String toString() {
        return "MethodeConnection{" +
                "slot=" + System.identityHashCode(slot) +
                ", fileSystemAdmin=" + System.identityHashCode(fileSystemAdmin) +
                ", session=" + System.identityHashCode(session) +
                ", repository=" + System.identityHashCode(repository) +
                ", namingService=" + System.identityHashCode(namingService) +
                ", orb=" + System.identityHashCode(orb) +
                '}';
    }
}
