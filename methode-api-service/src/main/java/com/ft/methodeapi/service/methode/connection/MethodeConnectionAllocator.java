package com.ft.methodeapi.service.methode.connection;

import EOM.FileSystemAdmin;
import EOM.Repository;
import EOM.Session;
import com.ft.methodeapi.metrics.FTTimer;
import com.ft.methodeapi.metrics.RunningTimer;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stormpot.Allocator;
import stormpot.Slot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MethodeConnectionAllocator
 *
 * @author Simon.Gibbs
 */
public class MethodeConnectionAllocator implements Allocator<MethodeConnection> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeConnectionAllocator.class);

    private final FTTimer allocationTimer = FTTimer.newTimer(MethodeConnectionAllocator.class, "allocate-connection");
    private final FTTimer deallocationTimer = FTTimer.newTimer(MethodeConnectionAllocator.class, "deallocate-connection");

    private final Gauge<Integer> deallocationQueueLength = Metrics.newGauge(MethodeConnectionAllocator.class,"length","deallocationQueue",new Gauge<Integer>() {
        @Override
        public Integer value() {
            return queueSize.get();
        }
    });

    private final MethodeObjectFactory implementation;
    private final ExecutorService executorService;

    private AtomicInteger queueSize = new AtomicInteger(0);

    public MethodeConnectionAllocator(MethodeObjectFactory implementation, ExecutorService executorService) {
        this.implementation = implementation;
        this.executorService = executorService;
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
    public void deallocate(final MethodeConnection connection) throws Exception {

        queueSize.incrementAndGet();
        executorService.submit(new Runnable() {
            @Override
            public void run() {

                RunningTimer timer = deallocationTimer.start();
                try {
                    implementation.maybeCloseFileSystemAdmin(connection.getFileSystemAdmin());
                    implementation.maybeCloseSession(connection.getSession());
                    implementation.maybeCloseRepository(connection.getRepository());
                    implementation.maybeCloseNamingService(connection.getNamingService());
                    implementation.maybeCloseOrb(connection.getOrb());
                    LOGGER.debug("Deallocated objects: {}",connection.toString());
                } catch (Exception e) {
                    // the Pool implementation is known to swallow exceptions.
                    LOGGER.warn("Deallocation did not complete cleanly",e);
                    throw e;
                } finally {
                    timer.stop();
                    queueSize.decrementAndGet();
                }
            }
        });


    }

    public int getQueueSize() {
        return deallocationQueueLength.value();
    }

}
