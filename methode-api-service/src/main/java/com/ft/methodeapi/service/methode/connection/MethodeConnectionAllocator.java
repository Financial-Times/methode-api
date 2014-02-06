package com.ft.methodeapi.service.methode.connection;

import EOM.FileSystemAdmin;
import EOM.Repository;
import EOM.Session;
import com.ft.methodeapi.metrics.FTTimer;
import com.ft.methodeapi.metrics.RunningTimer;
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

            MethodeConnection connection = new MethodeConnection(slot, orb, namingService, repository, session, fileSystemAdmin);
            LOGGER.debug("Allocated objects: {}",connection.toString());
            return connection;
        } finally {
            timer.stop();
        }
    }

    @Override
    public void deallocate(MethodeConnection connection) throws Exception {

        RunningTimer timer = deallocationTimer.start();
        try {
            implementation.maybeCloseFileSystemAdmin(connection.getFileSystemAdmin());
            implementation.maybeCloseSession(connection.getSession());
            implementation.maybeCloseRepository(connection.getRepository());
            implementation.maybeCloseNamingService(connection.getNamingService());
            implementation.maybeCloseOrb(connection.getOrb());
            LOGGER.debug("Deallocated objects: {}",connection.toString());
        } finally {
            timer.stop();
        }

    }

}
