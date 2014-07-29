package com.ft.methodeapi.service.methode.connection;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.ISOPeriodFormat;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;

import stormpot.Poolable;
import stormpot.Slot;
import EOM.FileSystemAdmin;
import EOM.Repository;
import EOM.Session;

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
    private final DateTime creationTimestamp;

    public MethodeConnection(Slot slot, ORB orb, NamingContextExt namingService, Repository repository, Session session, FileSystemAdmin fileSystemAdmin ) {
        this.slot = slot;
        this.fileSystemAdmin = fileSystemAdmin;
        this.session = session;
        this.repository = repository;
        this.namingService = namingService;
        this.orb = orb;
        this.creationTimestamp = DateTime.now();
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
    
    public int getAgeInSeconds() {
    	return Seconds.secondsBetween(creationTimestamp, DateTime.now()).getSeconds();
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
                ", age=" + getAgeInSeconds() +
                '}';
    }
}
