package com.ft.methodeapi.service.methode.connection;

import EOM.FileSystemAdmin;
import EOM.Repository;
import EOM.Session;
import com.ft.methodeapi.metrics.FTTimer;
import com.ft.methodeapi.metrics.RunningTimer;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stormpot.Allocator;
import stormpot.Slot;

/**
 * MethodeConnectionAllocator
 *
 * @author Simon.Gibbs
 */
public class MethodeConnectionAllocator implements Allocator<MethodeConnection> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeConnectionAllocator.class);

    private final FTTimer allocationTimer = FTTimer.newTimer(MethodeConnectionAllocator.class, "allocate-connection");
    private final FTTimer deallocationTimer = FTTimer.newTimer(MethodeConnectionAllocator.class, "deallocate-connection");
    private final MethodeObjectFactory implementation;

    public MethodeConnectionAllocator(MethodeObjectFactory implementation) {
        this.implementation = implementation;
    }

    @Override
    public MethodeConnection allocate(Slot slot) throws Exception {

        RunningTimer timer = allocationTimer.start();
        try {
            ORB orb = implementation.createOrb();
            NamingContextExt namingService = implementation.createNamingService(orb);
            Repository repository = implementation.createRepository(namingService);
            Session session = implementation.createSession(repository);
            FileSystemAdmin fileSystemAdmin = implementation.createFileSystemAdmin(session);

            return new MethodeConnection(slot, orb, namingService, repository, session, fileSystemAdmin);
        } finally {
            timer.stop();
        }
    }

    @Override
    public void deallocate(MethodeConnection methodeContext) throws Exception {

        RunningTimer timer = deallocationTimer.start();
        try {
            implementation.maybeCloseFileSystemAdmin(methodeContext.getFileSystemAdmin());
            implementation.maybeCloseSession(methodeContext.getSession());
            implementation.maybeCloseRepository(methodeContext.getRepository());
            implementation.maybeCloseNamingService(methodeContext.getNamingService());
            implementation.maybeCloseOrb(methodeContext.getOrb());
        } finally {
            timer.stop();
        }

    }

}
