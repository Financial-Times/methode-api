package com.ft.methodeapi.service.methode.connection;

import EOM.FileSystemAdmin;
import EOM.Repository;
import EOM.Session;
import com.ft.methodeapi.service.methode.MethodeException;
import com.ft.methodeapi.service.methode.monitoring.GaugeRangeHealthCheck;
import com.ft.timer.FTTimer;
import com.ft.timer.RunningTimer;
import com.google.common.base.Preconditions;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.HealthCheck;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stormpot.Allocator;
import stormpot.Config;
import stormpot.LifecycledResizablePool;
import stormpot.PoolException;
import stormpot.Timeout;
import stormpot.qpool.QueuePool;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * PoolingMethodeObjectFactory
 *
 * @author Simon.Gibbs
 */
public class PoolingMethodeObjectFactory implements MethodeObjectFactory, Managed {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoolingMethodeObjectFactory.class);

    private final FTTimer claimConnectionTimer = FTTimer.newTimer(PoolingMethodeObjectFactory.class, "claim-connection");
    private final FTTimer releaseConnectionTimer = FTTimer.newTimer(PoolingMethodeObjectFactory.class, "release-connection");

    ThreadLocal<MethodeConnection> allocatedConnection = new ThreadLocal<MethodeConnection>() {
        @Override
        protected MethodeConnection initialValue() {

            RunningTimer timer = claimConnectionTimer.start();
            try {
                LOGGER.debug("Claiming MethodeConnection");
                MethodeConnection connection = pool.claim(claimTimeout);
                if(connection==null) {
                    throw new MethodeException("Timeout after upon claiming MethodeConnection");
                }
                LOGGER.debug("Claimed objects: {}",connection);

                return connection;
            } catch (InterruptedException | PoolException e) {
                throw new MethodeException(e);
            } finally {
                timer.stop();
            }
        }
    };

    private final MethodeObjectFactory implementation;
    private final LifecycledResizablePool<MethodeConnection> pool;
    private final Timeout claimTimeout;

    private final Gauge<Integer> deallocationQueueLength;

    public PoolingMethodeObjectFactory(final MethodeObjectFactory implementation, ScheduledExecutorService executorService, PoolConfiguration configuration) {

        Preconditions.checkArgument(configuration != null, "Not configured");
        Preconditions.checkArgument(implementation != null, "PoolingMethodeObjectFactory must wrap another MethodeObjectFactory");
        Preconditions.checkArgument(executorService != null, "A scheduling thread pool service is required");

        assert configuration != null; // suppresses warning
        Preconditions.checkArgument(configuration.getSize()>0,"Pool size must be a positive integer");

        this.implementation = implementation;

        final MethodeConnectionAllocator allocator = new MethodeConnectionAllocator(implementation,executorService);

        deallocationQueueLength = Metrics.newGauge(MethodeConnectionAllocator.class, "length", "deallocationQueue", new Gauge<Integer>() {
            @Override
            public Integer value() {
                return allocator.getQueueSize();
            }
        });

        Config<MethodeConnection> poolConfig = new Config<MethodeConnection>().setAllocator(allocator);
        poolConfig.setSize(configuration.getSize());
        poolConfig.setExpiration(new TimeSpreadOrMethodeConnectionInvalidExpiration(5, 10, TimeUnit.MINUTES));

        pool = new SelfCleaningPool<>(new QueuePool<>(poolConfig), executorService, RecoverableAllocationException.class);
        claimTimeout = new Timeout(
                configuration.getTimeout().getQuantity(),
                configuration.getTimeout().getUnit()
            );

    }

    @Override
    public ORB createOrb() {
        return allocatedConnection.get().getOrb();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public NamingContextExt createNamingService(ORB orb) {
        Preconditions.checkState(orb==this.createOrb());
        return allocatedConnection.get().getNamingService();
    }

    @Override
    public Repository createRepository(NamingContextExt namingService) {
        return allocatedConnection.get().getRepository();
    }

    @Override
    public Session createSession(Repository repository) {
        return allocatedConnection.get().getSession();
    }

    @Override
    public FileSystemAdmin createFileSystemAdmin(Session session) {
        return allocatedConnection.get().getFileSystemAdmin();
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
        RunningTimer timer = releaseConnectionTimer.start();
        try {
            Preconditions.checkState(orb==this.createOrb());
            MethodeConnection connection = allocatedConnection.get();
            LOGGER.debug("Releasing objects: {}", connection);
            connection.release();

            // Ensure we release the thread local, otherwise we break the contract with the pool
            allocatedConnection.remove();
        } finally {
            timer.stop();
        }
    }

    @Override
    public String getDescription() {
        return String.format("[%d x [%s]]",pool.getTargetSize(),implementation.getDescription());
    }

    @Override
    public void start() throws Exception {
        // not used
    }

    @Override
    public void stop() throws Exception {
        LOGGER.info("Shutdown Requested");
        pool.shutdown().await(new Timeout(20, TimeUnit.SECONDS));
        LOGGER.info("Shutdown Complete");
    }

    @Override @SuppressWarnings("unchecked")
    public List<HealthCheck> createHealthChecks() {
        HealthCheck check = new DeallocationQueueSizeHealthCheck(this.getName(),deallocationQueueLength,pool.getTargetSize());
        return Collections.singletonList(check);
    }

    @Override
    public boolean isPooling() {
        return true;
    }

    @Override
    public String getName() {
        return implementation.getName();
    }
}
