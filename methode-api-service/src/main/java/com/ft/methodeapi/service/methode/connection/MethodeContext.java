package com.ft.methodeapi.service.methode.connection;

import EOM.Repository;
import EOM.Session;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import stormpot.Poolable;
import stormpot.Slot;

/**
 * MethodeContext
 *
 * @author Simon.Gibbs
 */
public class MethodeContext implements Poolable {

    private final Slot slot;
    private final Session session;
    private final Repository repository;
    private final NamingContextExt namingService;
    private final ORB orb;

    public MethodeContext(Slot slot, ORB orb, NamingContextExt namingService, Repository repository, Session session) {
        this.slot = slot;
        this.session = session;
        this.repository = repository;
        this.namingService = namingService;
        this.orb = orb;
    }

    @Override
    public void release() {
        slot.release(this);
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
}
