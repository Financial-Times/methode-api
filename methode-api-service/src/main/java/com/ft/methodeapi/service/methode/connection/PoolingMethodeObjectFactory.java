package com.ft.methodeapi.service.methode.connection;

import EOM.FileSystemAdmin;
import EOM.Repository;
import EOM.Session;
import com.ft.methodeapi.service.methode.MethodeException;
import com.google.common.base.Preconditions;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stormpot.Allocator;
import stormpot.Config;
import stormpot.PoolException;
import stormpot.Slot;
import stormpot.Timeout;
import stormpot.bpool.BlazePool;

import java.util.concurrent.TimeUnit;

/**
 * PoolingMethodeObjectFactory
 *
 * @author Simon.Gibbs
 */
public class PoolingMethodeObjectFactory implements MethodeObjectFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoolingMethodeObjectFactory.class);

    private final MetricsRegistry metricsRegistry = Metrics.defaultRegistry();

    private final Timer claimConnectionTimer = metricsRegistry.newTimer(PoolingMethodeObjectFactory.class, "claim-connection");
    private final Timer releaseConnectionTimer = metricsRegistry.newTimer(PoolingMethodeObjectFactory.class, "release-connection");
    private final Timer allocationTimer = metricsRegistry.newTimer(PoolingMethodeObjectFactory.class, "allocate-connection");
    private final Timer deallocationTimer = metricsRegistry.newTimer(PoolingMethodeObjectFactory.class, "deallocate-connection");

    ThreadLocal<MethodeConnection> allocatedContext = new ThreadLocal<MethodeConnection>() {
        @Override
        protected MethodeConnection initialValue() {

            TimerContext context = claimConnectionTimer.time();
            try {
                return pool.claim(timeout);
            } catch (InterruptedException | PoolException e) {
                throw new MethodeException(e);
            } finally {
                context.stop();
            }
        }
    };

    private final MethodeObjectFactory implementation;
    private final BlazePool<MethodeConnection> pool;
    private final Timeout timeout;


    public PoolingMethodeObjectFactory(final MethodeObjectFactory implementation, int poolSize) {
        Allocator<MethodeConnection> allocator = new Allocator<MethodeConnection>() {

            @Override
            public MethodeConnection allocate(Slot slot) throws Exception {

                TimerContext timer = allocationTimer.time();
                try {
                    LOGGER.info("Allocating MethodeConnection");

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
                TimerContext timer = deallocationTimer.time();
                try {
                    implementation.maybeCloseFileSystemAdmin(methodeContext.getFileSystemAdmin());
                    implementation.maybeCloseSession(methodeContext.getSession());
                    implementation.maybeCloseRepository(methodeContext.getRepository());
                    implementation.maybeCloseNamingService(methodeContext.getNamingService());
                    implementation.maybeCloseOrb(methodeContext.getOrb());

                    LOGGER.info("Deallocated MethodeConnection");
                } finally {
                    timer.stop();
                }

            }
        };

        Config<MethodeConnection> config = new Config<MethodeConnection>().setAllocator(allocator);
        config.setSize(poolSize);
        config.setExpiration(new TimeSpreadExpiration(5,10,TimeUnit.MINUTES));

        pool = new BlazePool<>(config);
        timeout = new Timeout(10, TimeUnit.SECONDS);
        this.implementation = implementation;

    }

    @Override
    public ORB createOrb() {
        return allocatedContext.get().getOrb();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public NamingContextExt createNamingService(ORB orb) {
        Preconditions.checkState(orb==this.createOrb());
        return allocatedContext.get().getNamingService();
    }

    @Override
    public Repository createRepository(NamingContextExt namingService) {
        return allocatedContext.get().getRepository();
    }

    @Override
    public Session createSession(Repository repository) {
        return allocatedContext.get().getSession();
    }

    @Override
    public FileSystemAdmin createFileSystemAdmin(Session session) {
        return allocatedContext.get().getFileSystemAdmin();
    }

    @Override
    public void maybeCloseFileSystemAdmin(FileSystemAdmin fileSystemAdmin) {
        // deferred to allocator
    }

    @Override
    public void maybeCloseSession(Session session) {
        // deferred to allocator
    }

    @Override
    public void maybeCloseRepository(Repository repository) {
        // deferred to allocator
    }

    @Override
    public void maybeCloseNamingService(NamingContextExt namingService) {
        // deferred to allocator
    }

    @Override
    public void maybeCloseOrb(ORB orb) {
        TimerContext timer = releaseConnectionTimer.time();
        try {
            Preconditions.checkState(orb==this.createOrb());
            allocatedContext.get().release();
            allocatedContext.remove();
        } finally {
            timer.stop();
        }
    }

    @Override
    public String getDescription() {
        return String.format("[%d x [%s]]",pool.getTargetSize(),implementation.getDescription());
    }
}
